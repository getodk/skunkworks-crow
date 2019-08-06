package org.odk.share.views.ui.bluetooth;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.odk.share.utilities.PermissionUtils;
import org.odk.share.views.ui.common.injectable.InjectableActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

import static org.odk.share.utilities.PermissionUtils.APP_SETTING_REQUEST_CODE;

/**
 * Receive activity, for testing, needs refactor.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
@RuntimePermissions
public class BtReceiverActivity extends InjectableActivity implements
        BluetoothReceiver.BluetoothReceiverListener, BluetoothListAdapter.OnDeviceClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btn_refresh)
    Button btnRefresh;

    @BindView(R.id.list_bt_device)
    RecyclerView recyclerView;

    @BindView(R.id.no_devices_view)
    View emptyDevicesView;

    @Inject
    ReceiverService receiverService;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothReceiver bluetoothReceiver;
    private final BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(this);
    private boolean isConnected = false;
    private ProgressDialog progressDialog;
    private ProgressDialog scanningDialog;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_receive);
        ButterKnife.bind(this);
        initEvents();

        setTitle(getString(R.string.connect_bluetooth_title));
        setSupportActionBar(toolbar);

        // checking for if bluetooth enabled
        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.enableBluetooth();
            BtReceiverActivityPermissionsDispatcher.updateDeviceListWithPermissionCheck(this);
        }

        setupScanningDialog();
    }

    /**
     * Init the basic events for our views.
     */
    private void initEvents() {
        progressDialog = new ProgressDialog(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bluetoothListAdapter);
        bluetoothReceiver = new BluetoothReceiver(this, this);

        BtReceiverActivityPermissionsDispatcher.updateDeviceListWithPermissionCheck(this);
    }

    /**
     * build a new progress dialog waiting for the scanning progress.
     */
    private void setupScanningDialog() {
        scanningDialog = new ProgressDialog(this);
        scanningDialog.setCancelable(false);
        scanningDialog.setTitle(getString(R.string.scanning_title));
        scanningDialog.setMessage(getString(R.string.scanning_msg));
        scanningDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop), (DialogInterface dialog, int which) -> {
            dialog.dismiss();
            bluetoothAdapter.cancelDiscovery();
        });
    }

    /**
     * Rescan the bluetooth devices and update the list.
     */
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void updateDeviceList() {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
        bluetoothListAdapter.notifyDataSetChanged();
    }

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
        PermissionUtils.showAppInfo(this, getPackageName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BtReceiverActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * Clicking to refresh the devices list.
     */
    @OnClick(R.id.btn_refresh)
    public void refresh() {
        if (BluetoothUtils.isBluetoothEnabled()) {
            BtReceiverActivityPermissionsDispatcher.updateDeviceListWithPermissionCheck(this);
        } else {
            BluetoothUtils.enableBluetooth();
            Toast.makeText(this, "bluetooth has been disabled, turning on...", Toast.LENGTH_SHORT).show();
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
                            progressDialog.setMessage(getString(R.string.connected_bluetooth_downloading));
                            isConnected = true;
                            break;
                        case DISCONNECTED:
                            isConnected = false;
                            break;
                    }
                });
    }

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
                            progressDialog.setTitle(getString(R.string.receiving_title));
                            progressDialog.setMessage(alertMsg);
                            break;
                        case FINISHED:
                            progressDialog.dismiss();
                            Toast.makeText(this, getString(R.string.tv_form_send_success), Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR:
                            progressDialog.dismiss();
                            Toast.makeText(this, getString(R.string.error_while_downloading, downloadEvent.getResult()), Toast.LENGTH_SHORT).show();
                            break;
                        case CANCELLED:
                            progressDialog.dismiss();
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
                            break;
                    }
                }, Timber::e);
    }

    /**
     * To check if the bluetooth devices list is empty, and present an empty view for users.
     */
    private void checkEmptyList() {
        if (bluetoothListAdapter.getItemCount() == 0) {
            emptyDevicesView.setVisibility(View.VISIBLE);
        } else {
            emptyDevicesView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        bluetoothListAdapter.addDevice(device);
        checkEmptyList();
    }

    @Override
    public void onDiscoveryStarted() {
        scanningDialog.show();
        checkEmptyList();
    }

    @Override
    public void onDiscoveryFinished() {
        scanningDialog.dismiss();
        checkEmptyList();
    }

    /**
     * Clicking the item to connect.
     */
    @Override
    public void onItemClick(BluetoothDevice device) {
        if (BluetoothUtils.isBluetoothEnabled()) {
            if (isConnected) {
                Toast.makeText(this, getString(R.string.dev_already_connected), Toast.LENGTH_SHORT).show();
            }

            receiverService.startBtDownloading(device.getAddress());
            progressDialog.setTitle(getString(R.string.connecting_title));
            progressDialog.setMessage(getString(R.string.connecting_message));
            progressDialog.setIndeterminate(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop), (DialogInterface dialog, int which) -> {
                receiverService.cancel();
                dialog.dismiss();
            });
            progressDialog.show();
        } else {
            Toast.makeText(this, getString(R.string.turning_on_bluetooth_message), Toast.LENGTH_SHORT).show();
            BluetoothUtils.enableBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_SETTING_REQUEST_CODE) {
            BtReceiverActivityPermissionsDispatcher.updateDeviceListWithPermissionCheck(this);
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
