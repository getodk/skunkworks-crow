package org.odk.share.views.ui.bluetooth;


import android.os.Bundle;
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
    TextView testTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @Inject
    SenderService senderService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();


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

        long[] formIds = getIntent().getLongArrayExtra(INSTANCE_IDS);
        int mode = getIntent().getIntExtra(MODE, ASK_REVIEW_MODE);
        if (formIds == null) {
            formIds = getIntent().getLongArrayExtra(FORM_IDS);
        }

        senderService.startUploading(formIds, mode);
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
                            Timber.d("======== SENDER: BLUETOOTH CONNECTED ========");
                            // TODO: update ui
                            break;
                        case DISCONNECTED:
                            Timber.d("======== SENDER: BLUETOOTH DISCONNECTED ========");
                            // TODO: update ui
                            break;
                    }
                });
    }

    // TODO: improve the UI/UX according to the callback.
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
                            //setDialogMessage(PROGRESS_DIALOG, alertMsg);
                            break;
                        case FINISHED:
                            //hideDialog(PROGRESS_DIALOG);
                            String result = uploadEvent.getResult();
                            //createAlertDialog(getString(R.string.transfer_result), result);
                            testTextView.setText(result);
                            Toast.makeText(this, getString(R.string.transfer_result) + " : " + result, Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR:
                            Toast.makeText(this, getString(R.string.error_while_uploading, uploadEvent.getResult()), Toast.LENGTH_SHORT).show();
                            //hideDialog(PROGRESS_DIALOG);
                            //createAlertDialog(getString(R.string.transfer_result), getString(R.string.error_while_uploading, uploadEvent.getResult()));
                            break;
                        case CANCELLED:
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
                            //hideDialog(PROGRESS_DIALOG);
                            break;
                    }
                }, Timber::e);
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


