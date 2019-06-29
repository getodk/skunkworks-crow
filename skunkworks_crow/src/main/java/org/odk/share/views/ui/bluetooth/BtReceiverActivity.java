package org.odk.share.views.ui.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.share.R;
import org.odk.share.bluetooth.BluetoothBasic;
import org.odk.share.bluetooth.BluetoothServer;
import org.odk.share.bluetooth.BluetoothUtils;
import org.odk.share.events.DownloadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.services.ReceiverService;
import org.odk.share.views.ui.common.injectable.InjectableActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Receive activity, for testing, needs refactor.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class BtReceiverActivity extends InjectableActivity implements BluetoothBasic.Listener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.test_text_view)
    TextView testTextView;

    @Inject
    ReceiverService receiverService;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    private BluetoothServer bluetoothServer;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_receive);
        ButterKnife.bind(this);

        setTitle(getString(R.string.receive_instance_title));
        setSupportActionBar(toolbar);

        bluetoothServer = new BluetoothServer(this);
        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.enableBluetooth();
        }

        receiverService.startDownloading();
    }

    @Override
    public void socketNotify(ConnectStatus status, Object obj) {
        if (BluetoothUtils.isActivityDestroyed(this)) {
            return;
        }

        String message;
        switch (status) {
            case CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                message = String.format("connected with device: %s(%s)", dev.getName(), dev.getAddress());
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case DISCONNECTED:
                bluetoothServer.listen();
                message = "lost connection, listen again...";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
        }
    }


    // TODO: improve the UI/UX progress.
    private Disposable addDownloadEventSubscription() {
        return rxEventBus.register(DownloadEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .subscribe(downloadEvent -> {
                    switch (downloadEvent.getStatus()) {
                        case QUEUED:
                            Toast.makeText(this, R.string.download_queued, Toast.LENGTH_SHORT).show();
                            break;
                        case DOWNLOADING:
                            int progress = downloadEvent.getCurrentProgress();
                            int total = downloadEvent.getTotalSize();
                            String alertMsg = getString(R.string.receiving_items, String.valueOf(progress), String.valueOf(total));
//                            progressDialog.setMessage(alertMsg);
                            Toast.makeText(this, alertMsg, Toast.LENGTH_SHORT).show();
                            break;
                        case FINISHED:
//                            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                            String result = downloadEvent.getResult();
                            Toast.makeText(this, getString(R.string.transfer_result) + " : " + result, Toast.LENGTH_SHORT).show();
//                            createAlertDialog(getString(R.string.transfer_result), result);
                            break;
                        case ERROR:
                            Toast.makeText(this, getString(R.string.error_while_downloading, downloadEvent.getResult()), Toast.LENGTH_SHORT).show();
//                            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
//                            createAlertDialog(getString(R.string.transfer_result), getString(R.string.error_while_downloading, downloadEvent.getResult()));
                            break;
                        case CANCELLED:
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
//                            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                            break;
                    }
                }, Timber::e);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compositeDisposable.add(addDownloadEventSubscription());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothServer.detachListener();
        bluetoothServer.close();
    }
}
