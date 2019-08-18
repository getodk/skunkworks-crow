package org.odk.share.views.ui.bluetooth;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import org.odk.share.R;
import org.odk.share.bluetooth.BluetoothUtils;
import org.odk.share.events.BluetoothEvent;
import org.odk.share.events.UploadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.services.SenderService;
import org.odk.share.utilities.DialogUtils;
import org.odk.share.utilities.PermissionUtils;
import org.odk.share.views.ui.common.injectable.InjectableActivity;
import org.odk.share.views.ui.hotspot.HpSenderActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

import static org.odk.share.utilities.ApplicationConstants.ASK_REVIEW_MODE;
import static org.odk.share.utilities.PermissionUtils.APP_SETTING_REQUEST_CODE;
import static org.odk.share.views.ui.instance.InstancesList.INSTANCE_IDS;
import static org.odk.share.views.ui.instance.fragment.ReviewedInstancesFragment.MODE;
import static org.odk.share.views.ui.send.fragment.BlankFormsFragment.FORM_IDS;


/**
 * Bluetooth sender activity.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
@RuntimePermissions
public class BtSenderActivity extends InjectableActivity {

    @BindView(R.id.test_text_view)
    TextView activityTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @Inject
    SenderService senderService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean isFinished = false;
    private boolean isDiscovering = false;

    private CountDownTimer countDownTimer;
    private ProgressDialog progressDialog;
    private AlertDialog resultDialog;
    private static final int CONNECT_TIMEOUT = 120;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private Intent receivedIntent;
    private static final int DISCOVERABLE_CODE = 0x121;
    private static final int SUCCESS_CODE = 120;

    private long[] formIds;
    private int mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_send);
        ButterKnife.bind(this);

        setTitle(" " + getString(R.string.send_instance_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.drawable.ic_bluetooth_white_24dp);
        }

        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.enableBluetooth();
        }

        if (getIntent() != null) {
            receivedIntent = getIntent();
        } else {
            throw new IllegalArgumentException("No received intent");
        }

        setupDialog();
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        formIds = getIntent().getLongArrayExtra(INSTANCE_IDS);
        mode = getIntent().getIntExtra(MODE, ASK_REVIEW_MODE);
        if (formIds == null) {
            formIds = receivedIntent.getLongArrayExtra(FORM_IDS);
        }

        if (BluetoothUtils.isBluetoothEnabled()) {
            BtSenderActivityPermissionsDispatcher.enableDiscoveryWithPermissionCheck(this);
        }
    }

    private void setupDialog() {
        //ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.sending_title));
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop),
                (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    senderService.cancel();
                    finish();
                });

        //AlertDialog
        resultDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.transfer_result))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.ok), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    senderService.cancel();
                    if (BluetoothUtils.isBluetoothEnabled()) {
                        BluetoothUtils.disableBluetooth();
                    }
                    finish();
                })
                .create();
    }

    /**
     * Create the switch method button in the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.switch_method_menu, menu);
        final MenuItem switchItem = menu.findItem(R.id.menu_switch);
        switchItem.setOnMenuItemClickListener((MenuItem item) -> {
            DialogUtils.createMethodSwitchDialog(this, (DialogInterface dialog, int which) -> {
                receivedIntent.setClass(this, HpSenderActivity.class);
                senderService.cancel();
                if (BluetoothUtils.isBluetoothEnabled()) {
                    BluetoothUtils.disableBluetooth();
                }
                startActivity(receivedIntent);
                finish();
            }).show();
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Creates a subscription for listening to all hotspot events being send through the
     * application's {@link RxEventBus}
     */
    private Disposable addBluetoothEventSubscription() {
        return rxEventBus.register(BluetoothEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .subscribe(bluetoothEvent -> {
                    switch (bluetoothEvent.getStatus()) {
                        case CONNECTED:
                            countDownTimer.cancel();
                            progressDialog.setMessage(getString(R.string.connecting_transfer_message));
                            progressDialog.show();
                            activityTextView.setVisibility(View.GONE);
                            break;
                        case DISCONNECTED:
                            progressDialog.dismiss();
                            break;
                    }
                });
    }

    private Disposable addUploadEventSubscription() {
        return rxEventBus.register(UploadEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .subscribe(uploadEvent -> {
                    switch (uploadEvent.getStatus()) {
                        case QUEUED:
                            isFinished = false;
                            Toast.makeText(this, R.string.upload_queued, Toast.LENGTH_SHORT).show();
                            break;
                        case UPLOADING:
                            int progress = uploadEvent.getCurrentProgress();
                            int total = uploadEvent.getTotalSize();
                            String alertMsg = getString(R.string.sending_items, String.valueOf(progress), String.valueOf(total));
                            progressDialog.setMessage(alertMsg);
                            break;
                        case FINISHED:
                            progressDialog.dismiss();
                            isFinished = true;
                            progressBar.setVisibility(View.GONE);
                            String result = uploadEvent.getResult();
                            resultDialog.setMessage(result);
                            resultDialog.show();
                            break;
                        case ERROR:
                            progressBar.setVisibility(View.GONE);
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(this, getString(R.string.error_while_uploading, uploadEvent.getResult()), Toast.LENGTH_SHORT).show();
                            break;
                        case CANCELLED:
                            progressBar.setVisibility(View.GONE);
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
                            break;
                    }
                }, Timber::e);
    }

    /**
     * Enable the bluetooth discovery for other devices. The timeout is specific seconds.
     */
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void enableDiscovery() {
        if (BluetoothUtils.isBluetoothEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // set the discovery timeout for 120s.
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, CONNECT_TIMEOUT);
            startActivityForResult(discoverableIntent, DISCOVERABLE_CODE);
        }
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON && !isDiscovering) {
                    BtSenderActivityPermissionsDispatcher.enableDiscoveryWithPermissionCheck(BtSenderActivity.this);
                }
            }
        }
    };

    /**
     * If the permission was denied, finishing this activity.
     */
    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void showDeniedForLocation() {
        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * If clicked the "never ask", we should show a toast to guide user.
     */
    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void showNeverAskForLocation() {
        PermissionUtils.showAppInfo(this, getPackageName(), getString(R.string.permission_open_location_info), getString(R.string.permission_location_denied));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BtSenderActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DISCOVERABLE_CODE:
                if (resultCode == SUCCESS_CODE) {
                    isDiscovering = true;
                    startCheckingDiscoverableDuration();
                } else {
                    isDiscovering = false;
                    finish();
                }
                break;
            case APP_SETTING_REQUEST_CODE:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    PermissionUtils.showAppInfo(this, getPackageName(), getString(R.string.permission_open_location_info), getString(R.string.permission_location_denied));
                } else {
                    BtSenderActivityPermissionsDispatcher.enableDiscoveryWithPermissionCheck(this);
                }
                break;
        }
    }

    /**
     * Checking the discoverable time, if the device is no longer discoverable, we should show
     * an {@link AlertDialog} to notice our users.
     */
    private void startCheckingDiscoverableDuration() {
        senderService.startUploading(formIds, mode);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.timeout))
                .setMessage(getString(R.string.bluetooth_send_time_up))
                .setCancelable(false)
                .setNegativeButton(R.string.quit, (DialogInterface dialog, int which) -> {
                    finish();
                })
                .create();

        activityTextView.setText(getString(R.string.tv_sender_wait_for_connect));
        countDownTimer = new CountDownTimer(CONNECT_TIMEOUT * COUNT_DOWN_INTERVAL, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                activityTextView.setText(String.format(getString(R.string.tv_sender_wait_for_connect),
                        String.valueOf(millisUntilFinished / COUNT_DOWN_INTERVAL)));
            }

            @Override
            public void onFinish() {
                isDiscovering = false;
                if (!(BtSenderActivity.this).isFinishing()) {
                    alertDialog.show();
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compositeDisposable.add(addUploadEventSubscription());
        compositeDisposable.add(addBluetoothEventSubscription());
    }

    @Override
    protected void onPause() {
        super.onPause();
        compositeDisposable.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    @Override
    public void onBackPressed() {
        if (!isFinished) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.stop_sending))
                    .setMessage(getString(R.string.stop_sending_msg))
                    .setPositiveButton(R.string.stop, (DialogInterface dialog, int which) -> {
                        senderService.cancel();
                        BluetoothUtils.disableBluetooth();
                        super.onBackPressed();
                    })
                    .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.disable_bluetooth))
                    .setMessage(getString(R.string.disable_bluetooth_sender_msg))
                    .setPositiveButton(R.string.quit, (DialogInterface dialog, int which) -> {
                        senderService.cancel();
                        BluetoothUtils.disableBluetooth();
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }
    }
}