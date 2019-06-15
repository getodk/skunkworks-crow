package org.odk.share.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import timber.log.Timber;

/**
 * Server for bluetooth socket.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 04/06/2019
 */
public class BluetoothServer extends BluetoothBasic {
    private static final String TAG = BluetoothServer.class.getSimpleName();
    private BluetoothServerSocket serverSocket;

    public BluetoothServer(Listener listener) {
        super(listener);
        listen();
    }

    /**
     * Listen for connections initiated by the client.
     */
    public void listen() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            //serverSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //Encrypted transmission, Android system forced pairing, pop-up window display pairing code.
            serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG, SPP_UUID); //Clear text transmission (unsafe), no need to pair.
            BluetoothUtils.EXECUTOR.execute(() -> {
                try {
                    BluetoothSocket socket = serverSocket.accept();
                    serverSocket.close();
                    loopRead(socket);
                } catch (Throwable e) {
                    close();
                }
            });
        } catch (Throwable e) {
            close();
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            serverSocket.close();
        } catch (Throwable e) {
            Timber.e(e);
        }
    }

}
