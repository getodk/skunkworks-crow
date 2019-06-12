package org.odk.share.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 04/06/2019
 */
public class BluetoothClient extends BluetoothBasic {

    BluetoothClient(Listener listener) {
        super(listener);
    }

    /**
     * Establish a long connection with the remote device.
     *
     * @param dev remote device
     */
    public void connect(BluetoothDevice dev) {
        close();
        try {
            // final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //Encrypted transmission, Android system forced pairing, pop-up window display pairing code.
            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //Clear text transmission (unsafe), no need to pair.
            BluetoothUtils.EXECUTOR.execute(() -> {
                loopRead(socket);
            });
        } catch (Throwable e) {
            close();
        }
    }
}
