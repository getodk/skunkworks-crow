package org.odk.share.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

import timber.log.Timber;


/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 04/06/2019
 */
public class BluetoothBasic {

    static final UUID SPP_UUID = UUID.fromString("724f00a0-795e-4272-a85a-11075e760e58");
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";

    // TODO: replace with single file instance.
    private static final int FLAG_MSG = 0; // Flag for messages.
    private static final int FLAG_FILE = 1; // Flag for files.

    private BluetoothSocket bluetoothSocket;
    private DataOutputStream dataOutputStream;
    private Listener listener;
    private boolean isRead;
    private boolean isSending;

    public BluetoothBasic(Listener listener) {
        this.listener = listener;
    }

    /**
     * Cycle through the other party's data (blocking wait if there is no data).
     * <p>
     * TODO: Refactor while disconnecting when received/dent files.
     */
    void loopRead(BluetoothSocket socket) {
        bluetoothSocket = socket;
        try {
            if (!bluetoothSocket.isConnected()) {
                bluetoothSocket.connect();
            }
            notifyUI(Listener.ConnectStatus.CONNECTED, bluetoothSocket.getRemoteDevice());
            dataOutputStream = new DataOutputStream(bluetoothSocket.getOutputStream());
            DataInputStream in = new DataInputStream(bluetoothSocket.getInputStream());
            isRead = true;
            while (isRead) { //using dead loop to read
                switch (in.readInt()) {
                    case FLAG_MSG: //read short messages.
                        String msg = in.readUTF();
                        notifyUI(Listener.ConnectStatus.MSG, "receive short message: " + msg);
                        break;
                    case FLAG_FILE: //Read files.
                        //TODO: create file path.
                        BluetoothUtils.mkdirs(FILE_PATH);
                        String fileName = in.readUTF(); //file name
                        long fileLen = in.readLong(); //file length
                        // Read file content.
                        long len = 0;
                        int r;
                        byte[] b = new byte[4 * 1024];
                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
                        notifyUI(Listener.ConnectStatus.MSG, "receiving file (" + fileName + "), please wait...");
                        while ((r = in.read(b)) != -1) {
                            out.write(b, 0, r);
                            len += r;
                            if (len >= fileLen) {
                                break;
                            }
                        }
                        notifyUI(Listener.ConnectStatus.MSG, "done (put file in: " + FILE_PATH + ")");
                        break;
                }
            }
        } catch (Throwable e) {
            close();
        }
    }

    /**
     * Send short messages.
     */
    public void sendMessage(String msg) {
        if (isSending()) {
            return;
        }
        isSending = true;
        try {
            dataOutputStream.writeInt(FLAG_MSG);
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush();
            notifyUI(Listener.ConnectStatus.MSG, "send short message: " + msg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }

    /**
     * Send files.
     */
    public void sendFile(final String filePath) {
        if (isSending()) {
            return;
        }
        isSending = true;
        BluetoothUtils.EXECUTOR.execute(() -> {
            try {
                FileInputStream in = new FileInputStream(filePath);
                File file = new File(filePath);
                dataOutputStream.writeInt(FLAG_FILE);
                dataOutputStream.writeUTF(file.getName());
                dataOutputStream.writeLong(file.length());
                int r;
                byte[] b = new byte[4 * 1024];
                notifyUI(Listener.ConnectStatus.MSG, "sending file (" + filePath + "), please wait...");
                while ((r = in.read(b)) != -1) {
                    dataOutputStream.write(b, 0, r);
                }
                dataOutputStream.flush();
                notifyUI(Listener.ConnectStatus.MSG, "done.");
            } catch (Throwable e) {
                close();
            }
            isSending = false;
        });
    }

    /**
     * Release listener references (e.g, release references to activities to avoid memory leaks).
     */
    public void detachListener() {
        listener = null;
    }

    /**
     * Close Socket connection.
     */
    public void close() {
        try {
            isRead = false;
            bluetoothSocket.close();
            notifyUI(Listener.ConnectStatus.DISCONNECTED, null);
        } catch (Throwable e) {
            Timber.e(e);
        }
    }

    /**
     * Determine whether the current device is connected to the specified device.
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (bluetoothSocket != null && bluetoothSocket.isConnected());
        if (dev == null) {
            return connected;
        }
        return connected && bluetoothSocket.getRemoteDevice().equals(dev);
    }

    private boolean isSending() {
        if (isSending) {
            Timber.i("Sending other data, please send it later...");
            return true;
        }
        return false;
    }

    private void notifyUI(final Listener.ConnectStatus status, final Object obj) {
        BluetoothUtils.runOnUi(() -> {
            try {
                if (listener != null) {
                    listener.socketNotify(status, obj);
                }
            } catch (Throwable e) {
                Timber.e(e);
            }
        });
    }

    public interface Listener {
        enum ConnectStatus {
            DISCONNECTED,
            CONNECTED,
            MSG
        }

        void socketNotify(Listener.ConnectStatus status, Object obj);
    }
}
