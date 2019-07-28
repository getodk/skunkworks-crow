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

    private final BluetoothReceiverListener bluetoothReceiverListener;

    public BluetoothReceiver(Context context, BluetoothReceiverListener bluetoothReceiverListener) {
        this.bluetoothReceiverListener = bluetoothReceiverListener;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //Bluetooth starts searching.
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //Bluetooth search ends.
        filter.addAction(BluetoothDevice.ACTION_FOUND); //Bluetooth discovers new devices (unpaired devices).
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                // if the discovery started, update the ui in activity.
                Timber.d("bluetooth devices discovery started...");
                bluetoothReceiverListener.onDiscoveryStarted();
                break;
            case BluetoothDevice.ACTION_FOUND:
                // once the bluetooth device was found, update the ui.
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothDevice != null) {
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                    Timber.d("EXTRA_RSSI: %s", rssi);
                    bluetoothReceiverListener.onDeviceFound(bluetoothDevice);
                }
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                // if the discovery finished, update the ui in activity.
                Timber.d("bluetooth devices discovery finished...");
                bluetoothReceiverListener.onDiscoveryFinished();
                break;
        }
    }

    /**
     * Listener for bluetooth devices when we found a new {@link BluetoothDevice}.
     */
    public interface BluetoothReceiverListener {
        void onDeviceFound(BluetoothDevice device);

        void onDiscoveryStarted();

        void onDiscoveryFinished();
    }
}
