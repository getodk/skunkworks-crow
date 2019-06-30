package org.odk.share.views.ui.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.share.R;
import org.odk.share.bluetooth.BluetoothReceiver;
import org.odk.share.bluetooth.BluetoothUtils;
import org.odk.share.events.BluetoothEvent;
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
public class BtReceiverActivity extends InjectableActivity implements
        BluetoothReceiver.Listener, BluetoothListAdapter.OnDeviceClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btn_refresh)
    Button btnRefresh;

    @BindView(R.id.list_bt_device)
    RecyclerView recyclerView;

    @Inject
    ReceiverService receiverService;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    private BluetoothReceiver bluetoothReceiver;
    private final BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(this);
    private boolean isConnected = false;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_receive);
        ButterKnife.bind(this);
        initEvents();

        setTitle(getString(R.string.receive_instance_title));
        setSupportActionBar(toolbar);

        // checking for if bluetooth enabled
        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.enableBluetooth();
            bluetoothListAdapter.rescan();
        }
    }

    private void initEvents() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bluetoothListAdapter);
        bluetoothReceiver = new BluetoothReceiver(this, this);
        BluetoothAdapter.getDefaultAdapter().startDiscovery();

        // click to refresh the devices list.
        btnRefresh.setOnClickListener((View v) -> {
            if (BluetoothUtils.isBluetoothEnabled()) {
                bluetoothListAdapter.rescan();
            } else {
                BluetoothUtils.enableBluetooth();
                Toast.makeText(this, "bluetooth has been disabled, turning on...", Toast.LENGTH_SHORT).show();
            }
        });
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
                            // TODO: update ui
                            Timber.d("======== RECEIVER: BLUETOOTH CONNECTED ========");
                            isConnected = true;
                            break;
                        case DISCONNECTED:
                            // TODO: update ui
                            Timber.d("======== RECEIVER: BLUETOOTH DISCONNECTED ========");
                            isConnected = false;
                            break;
                    }
                });
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
    public void foundDevice(BluetoothDevice device) {
        bluetoothListAdapter.addDevice(device);
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (BluetoothUtils.isBluetoothEnabled()) {
            if (isConnected) {
                Toast.makeText(this, "bluetooth already connected to another device", Toast.LENGTH_SHORT).show();
                return;
            }

            receiverService.startDownloading(dev.getAddress());
            // TODO: update ui
        } else {
            Toast.makeText(this, "you have disabled bluetooth, turning on...", Toast.LENGTH_SHORT).show();
            BluetoothUtils.enableBluetooth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        compositeDisposable.add(addDownloadEventSubscription());
        compositeDisposable.add(addBluetoothEventSubscription());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }
}
