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
import org.odk.share.bluetooth.BluetoothBasic;
import org.odk.share.bluetooth.BluetoothClient;
import org.odk.share.bluetooth.BluetoothReceiver;
import org.odk.share.bluetooth.BluetoothUtils;
import org.odk.share.views.ui.common.injectable.InjectableActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


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
                BluetoothDevice dev = (BluetoothDevice) obj;
                message = String.format("connected success with: %s(%s)", dev.getName(), dev.getAddress());
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                sendMsg();
                break;
            case DISCONNECTED:
                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                break;
            case MSG:
                message = String.format("\n%s", obj);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

            bluetoothClient.connect(dev);
            Toast.makeText(this, "connecting...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "you have disabled bluetooth, turning on...", Toast.LENGTH_SHORT).show();
            BluetoothUtils.enableBluetooth();
        }
    }


    //TODO: For testing, please remove this after connect to form instance.
    public void sendMsg() {
        if (bluetoothClient.isConnected(null)) {
            String message = "Hello World";
            bluetoothClient.sendMessage(message);
        } else {
            Toast.makeText(this, "no connections", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
        bluetoothClient.detachListener();
        bluetoothClient.close();
    }
}


