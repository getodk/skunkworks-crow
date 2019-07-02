package org.odk.share.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import timber.log.Timber;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 04/06/2019
 */
public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = BluetoothReceiver.class.getSimpleName();
    private final BluetoothReceiverListener bluetoothReceiverListener;

    public BluetoothReceiver(Context context, BluetoothReceiverListener bluetoothReceiverListener) {
        this.bluetoothReceiverListener = bluetoothReceiverListener;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Bluetooth switch status.
        filter.addAction(BluetoothDevice.ACTION_FOUND); //Bluetooth discovers new devices (unpaired devices).
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        Timber.d(TAG, action);
        BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (dev != null) {
            Timber.d("%s BluetoothDevice: %s, Address: %s", TAG, dev.getName(), dev.getAddress());
        }
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                Timber.d("%s STATE: %s", TAG, state);
                break;
            case BluetoothDevice.ACTION_FOUND:
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                Timber.d("EXTRA_RSSI: %s", rssi);
                bluetoothReceiverListener.onDeviceFound(dev);
                break;
        }
    }

    /**
     * Listener for bluetooth devices when we found a new {@link BluetoothDevice}.
     */
    public interface BluetoothReceiverListener {
        void onDeviceFound(BluetoothDevice device);
    }
}
