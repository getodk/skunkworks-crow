package org.odk.share.views.ui.bluetooth;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.share.R;
import org.odk.share.bluetooth.BluetoothUtils;
import org.odk.share.events.BluetoothEvent;
import org.odk.share.events.UploadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.services.SenderService;
import org.odk.share.utilities.DialogUtils;
import org.odk.share.views.ui.common.injectable.InjectableActivity;
import org.odk.share.views.ui.hotspot.HpSenderActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static org.odk.share.utilities.ApplicationConstants.ASK_REVIEW_MODE;
import static org.odk.share.views.ui.instance.InstancesList.INSTANCE_IDS;
import static org.odk.share.views.ui.instance.fragment.ReviewedInstancesFragment.MODE;
import static org.odk.share.views.ui.send.fragment.BlankFormsFragment.FORM_IDS;


/**
 * Send activity, for testing, needs refactor.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class BtSenderActivity extends InjectableActivity {

    @BindView(R.id.test_text_view)
    TextView resultTextView;

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
    private CountDownTimer countDownTimer;
    private static final int CONNECT_TIMEOUT = 120;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private BtSenderActivity thisActivity = this;
    private Intent receivedIntent;


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

        checkDiscoverableDuration();

        receivedIntent = getIntent();
        long[] formIds = receivedIntent.getLongArrayExtra(INSTANCE_IDS);
        int mode = receivedIntent.getIntExtra(MODE, ASK_REVIEW_MODE);
        if (formIds == null) {
            formIds = receivedIntent.getLongArrayExtra(FORM_IDS);
        }

        enableDiscovery();
        senderService.startUploading(formIds, mode);
    }

    /**
     * Create the switch method button in the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.switch_method_menu, menu);
        final MenuItem switchItem = menu.findItem(R.id.menu_switch);

        switchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DialogUtils.createMethodSwitchDialog(thisActivity, (DialogInterface dialog, int which) -> {
                    Intent intent = receivedIntent;
                    intent.setClass(thisActivity, HpSenderActivity.class);
                    senderService.cancel();
                    startActivity(intent);
                    finish();
                }).show();
                return true;
            }
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
                            resultTextView.setText(getString(R.string.connecting_transfer_message));
                            break;
                        case DISCONNECTED:
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
                            Toast.makeText(this, R.string.upload_queued, Toast.LENGTH_SHORT).show();
                            break;
                        case UPLOADING:
                            int progress = uploadEvent.getCurrentProgress();
                            int total = uploadEvent.getTotalSize();

                            String alertMsg = getString(R.string.sending_items, String.valueOf(progress), String.valueOf(total));
                            Toast.makeText(this, alertMsg, Toast.LENGTH_SHORT).show();
                            break;
                        case FINISHED:
                            progressBar.setVisibility(View.GONE);
                            String result = uploadEvent.getResult();
                            if (TextUtils.isEmpty(result)) {
                                resultTextView.setText(getString(R.string.tv_form_already_exist));
                            } else {
                                resultTextView.setText(getString(R.string.tv_form_send_success));
                                resultTextView.append(result);
                            }
                            Toast.makeText(this, getString(R.string.transfer_result) + " : " + result, Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR:
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, getString(R.string.error_while_uploading, uploadEvent.getResult()), Toast.LENGTH_SHORT).show();
                            break;
                        case CANCELLED:
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
                            break;
                    }
                }, Timber::e);
    }

    /**
     * Enable the bluetooth discovery for other devices. The timeout is specific seconds.
     */
    private void enableDiscovery() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // set the discovery timeout for 120s.
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, CONNECT_TIMEOUT);
        startActivity(discoverableIntent);
    }

    /**
     * Checking the discoverable time, if the device is no longer discoverable, we should show
     * an {@link AlertDialog} to notice our users.
     */
    private void checkDiscoverableDuration() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.timeout))
                .setMessage(getString(R.string.bluetooth_send_time_up))
                .setCancelable(false)
                .setNegativeButton(R.string.quit, (DialogInterface dialog, int which) -> {
                    finish();
                })
                .create();

        resultTextView.setText(getString(R.string.tv_sender_wait_for_connect));
        countDownTimer = new CountDownTimer(CONNECT_TIMEOUT * COUNT_DOWN_INTERVAL, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                resultTextView.setText(String.format(getString(R.string.tv_sender_wait_for_connect),
                        String.valueOf(millisUntilFinished / COUNT_DOWN_INTERVAL)));
            }

            @Override
            public void onFinish() {
                if (!BluetoothUtils.isActivityDestroyed(thisActivity)) {
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
}