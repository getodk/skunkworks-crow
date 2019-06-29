package org.odk.share.views.ui.bluetooth;


import android.app.ProgressDialog;
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
import org.odk.share.bluetooth.BluetoothBasic;
import org.odk.share.bluetooth.BluetoothClient;
import org.odk.share.bluetooth.BluetoothReceiver;
import org.odk.share.bluetooth.BluetoothUtils;
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
public class BtSenderActivity extends InjectableActivity implements BluetoothBasic.Listener,
        BluetoothReceiver.Listener, BluetoothListAdapter.OnDeviceClickListener {

    @BindView(R.id.btn_refresh)
    Button btnRefresh;

    @BindView(R.id.list_bt_device)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @Inject
    SenderService senderService;

    private long[] formIds;
    private int mode;
    private ProgressDialog connectingDialog;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(this);
    private final BluetoothClient bluetoothClient = new BluetoothClient(this);

    private BluetoothReceiver bluetoothReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_send);
        ButterKnife.bind(this);
        initEvents();

        setTitle(getString(R.string.connect_bluetooth_title));
        setSupportActionBar(toolbar);

        // checking for if bluetooth enabled
        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.enableBluetooth();
            bluetoothListAdapter.rescan();
        }

        formIds = getIntent().getLongArrayExtra(INSTANCE_IDS);
        mode = getIntent().getIntExtra(MODE, ASK_REVIEW_MODE);
        if (formIds == null) {
            formIds = getIntent().getLongArrayExtra(FORM_IDS);
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
     * get callback messages from bluetooth socket.
     */
    @Override
    public void socketNotify(ConnectStatus status, Object obj) {
        if (BluetoothUtils.isActivityDestroyed(this)) {
            return;
        }

        String message;
        switch (status) {
            case CONNECTED:
                connectingDialog.dismiss();
                BluetoothDevice dev = (BluetoothDevice) obj;
                message = String.format("connected success with: %s(%s)", dev.getName(), dev.getAddress());
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case DISCONNECTED:
                connectingDialog.dismiss();
                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void foundDevice(BluetoothDevice device) {
        bluetoothListAdapter.addDevice(device);
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (BluetoothUtils.isBluetoothEnabled()) {
            if (bluetoothClient.isConnected(dev)) {
                Toast.makeText(this, "already connected to this device!", Toast.LENGTH_SHORT).show();
                return;
            }

//            bluetoothClient.connect(dev);
            senderService.startBtUploading(formIds, mode);
            connectingDialog = ProgressDialog.show(this, "Connecting",
                    "Connecting to device, please wait...", true);
        } else {
            Toast.makeText(this, "you have disabled bluetooth, turning on...", Toast.LENGTH_SHORT).show();
            BluetoothUtils.enableBluetooth();
        }
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
//                            setDialogMessage(PROGRESS_DIALOG, alertMsg);
                            break;
                        case FINISHED:
//                            hideDialog(PROGRESS_DIALOG);
                            String result = uploadEvent.getResult();
//                            createAlertDialog(getString(R.string.transfer_result), result);
                            Toast.makeText(this, getString(R.string.transfer_result) + " : " + result, Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR:
                            Toast.makeText(this, getString(R.string.error_while_uploading, uploadEvent.getResult()), Toast.LENGTH_SHORT).show();
//                            hideDialog(PROGRESS_DIALOG);
//                            createAlertDialog(getString(R.string.transfer_result), getString(R.string.error_while_uploading, uploadEvent.getResult()));
                            break;
                        case CANCELLED:
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
//                            hideDialog(PROGRESS_DIALOG);
                            break;
                    }
                }, Timber::e);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compositeDisposable.add(addUploadEventSubscription());
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
        bluetoothClient.detachListener();
        bluetoothClient.close();
    }
}


