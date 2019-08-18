package org.odk.share.views.ui.bluetooth;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import org.odk.share.utilities.ActivityUtils;
import org.odk.share.utilities.DialogUtils;
import org.odk.share.utilities.PermissionUtils;
import org.odk.share.views.ui.common.injectable.InjectableActivity;
import org.odk.share.views.ui.hotspot.HpReceiverActivity;

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
 * Bluetooth receiver activity.
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
    private AlertDialog resultDialog;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_receive);
        ButterKnife.bind(this);

        setTitle(" " + getString(R.string.connect_bluetooth_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.drawable.ic_bluetooth_white_24dp);
        }

        initEvents();
        setupDialogs();
    }

    /**
     * Init the basic events for our views.
     */
    private void initEvents() {
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
    private void setupDialogs() {
        //scanning dialog
        scanningDialog = new ProgressDialog(this);
        scanningDialog.setCancelable(false);
        scanningDialog.setTitle(getString(R.string.scanning_title));
        scanningDialog.setMessage(getString(R.string.scanning_msg));
        scanningDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop), (DialogInterface dialog, int which) -> {
            dialog.dismiss();
            bluetoothAdapter.cancelDiscovery();
        });

        //result dialog
        resultDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.transfer_result))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.ok), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    receiverService.cancel();
                    if (BluetoothUtils.isBluetoothEnabled()) {
                        BluetoothUtils.disableBluetooth();
                    }
                    finish();
                })
                .create();
    }

    /**
     * Rescan the bluetooth devices and update the list.
     */
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void updateDeviceList() {
        if (!BluetoothUtils.isBluetoothEnabled()) {
            Toast.makeText(this, getString(R.string.turning_on_bluetooth_message), Toast.LENGTH_SHORT).show();
            BluetoothUtils.enableBluetooth();
        }
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
        PermissionUtils.showAppInfo(this, getPackageName(), getString(R.string.permission_open_location_info), getString(R.string.permission_location_denied));
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
            Toast.makeText(this, getString(R.string.turning_on_bluetooth_message), Toast.LENGTH_SHORT).show();
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
                            if (progressDialog != null) {
                                progressDialog.setMessage(getString(R.string.connected_bluetooth_downloading));
                            }
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
                            Timber.d(getString(R.string.download_queued));
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
                            String result = downloadEvent.getResult();
                            resultDialog.setMessage(result);
                            resultDialog.show();
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
        bluetoothListAdapter.clearBluetoothDeviceList();
    }

    @Override
    public void onDiscoveryFinished() {
        scanningDialog.dismiss();
        checkEmptyList();
    }

    @Override
    public void onStateChanged(int state) {
        if (state == BluetoothAdapter.STATE_ON) {
            BtReceiverActivityPermissionsDispatcher.updateDeviceListWithPermissionCheck(this);
        }
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
            progressDialog = new ProgressDialog(this);
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

    /**
     * Create the switch method button in the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.switch_method_menu, menu);
        final MenuItem switchItem = menu.findItem(R.id.menu_switch);
        switchItem.setOnMenuItemClickListener((MenuItem item) -> {
            DialogUtils.createMethodSwitchDialog(this, (DialogInterface dialog, int which) -> {
                receiverService.cancel();
                if (BluetoothUtils.isBluetoothEnabled()) {
                    BluetoothUtils.disableBluetooth();
                }
                ActivityUtils.launchActivity(this, HpReceiverActivity.class, true);
            }).show();
            return true;
        });
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onBackPressed() {
        if (BluetoothUtils.isBluetoothEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.disable_bluetooth))
                    .setMessage(getString(R.string.disable_bluetooth_receiver_msg))
                    .setPositiveButton(R.string.quit, (DialogInterface dialog, int which) -> {
                        receiverService.cancel();
                        BluetoothUtils.disableBluetooth();
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
