package org.odk.share.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
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
    private final Listener listener;

    public BluetoothReceiver(Context context, Listener listener) {
        this.listener = listener;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Bluetooth switch status.
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //Bluetooth starts searching.
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //Bluetooth search ends.

        filter.addAction(BluetoothDevice.ACTION_FOUND); //Bluetooth discovers new devices (unpaired devices).
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST); //Before the system pops up the match box (confirm / enter the pairing code).
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //Device pairing status change.
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //Bottom connection establishment.
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //The bottommost connection is broken.

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter connection status.
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); //BluetoothHeadset connection status.
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp connection status.
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        Timber.d(TAG + "===>" + action);
        BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (dev != null) {
            Timber.d(TAG + "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
        }
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                Timber.d(TAG + "STATE: " + state);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                break;

            case BluetoothDevice.ACTION_FOUND:
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                Timber.d(TAG + "EXTRA_RSSI:" + rssi);
                listener.foundDevice(dev);
                break;
            case BluetoothDevice.ACTION_PAIRING_REQUEST: //Automatic pairing is cancelled before the system pops up the matching box, and the system matching box is cancelled.
                /*try {
                    abortBroadcast();//End pairing broadcast, cancel system pairing box.
                    boolean ret = dev.setPin("1234".getBytes()); //Set the PIN pairing code (must be fixed).
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                Timber.i(TAG + "BOND_STATE: " + intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0));
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                break;
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                Timber.i(TAG + "CONN_STATE: " + intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0));
                break;
            case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                Timber.i(TAG + "CONN_STATE: " + intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0));
                break;
            case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                Timber.i(TAG + "CONN_STATE: " + intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, 0));
                break;
        }
    }

    public interface Listener {
        void foundDevice(BluetoothDevice device);
    }
}
