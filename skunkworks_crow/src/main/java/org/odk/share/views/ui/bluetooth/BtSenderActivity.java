package org.odk.share.views.ui.bluetooth;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
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
import org.odk.share.views.ui.common.injectable.InjectableActivity;

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
    TextView activityTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @Inject
    SenderService senderService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CountDownTimer countDownTimer;
    private ProgressDialog progressDialog;
    private AlertDialog resultDialog;
    private static final int CONNECT_TIMEOUT = 120;
    private static final int COUNT_DOWN_INTERVAL = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_send);
        ButterKnife.bind(this);

        setTitle(getString(R.string.send_instance_title));
        setSupportActionBar(toolbar);

        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.enableBluetooth();
        }

        setupDialog();
        checkDiscoverableDuration();

        long[] formIds = getIntent().getLongArrayExtra(INSTANCE_IDS);
        int mode = getIntent().getIntExtra(MODE, ASK_REVIEW_MODE);
        if (formIds == null) {
            formIds = getIntent().getLongArrayExtra(FORM_IDS);
        }

        enableDiscovery();
        senderService.startUploading(formIds, mode);
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
                    finish();
                })
                .create();
    }

    private void dismissAllDialogs() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (resultDialog != null && resultDialog.isShowing()) {
            resultDialog.dismiss();
        }
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
                            String result = uploadEvent.getResult();
                            resultDialog.setMessage(result);
                            resultDialog.show();
                            break;
                        case ERROR:
                            dismissAllDialogs();
                            Toast.makeText(this, getString(R.string.error_while_uploading, uploadEvent.getResult()), Toast.LENGTH_SHORT).show();
                            break;
                        case CANCELLED:
                            dismissAllDialogs();
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

        activityTextView.setText(getString(R.string.tv_sender_wait_for_connect));
        countDownTimer = new CountDownTimer(CONNECT_TIMEOUT * COUNT_DOWN_INTERVAL, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                activityTextView.setText(String.format(getString(R.string.tv_sender_wait_for_connect),
                        String.valueOf(millisUntilFinished / COUNT_DOWN_INTERVAL)));
            }

            @Override
            public void onFinish() {
                alertDialog.show();
            }
        }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissAllDialogs();
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