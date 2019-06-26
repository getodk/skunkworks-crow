package org.odk.share.views.ui.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.share.R;
import org.odk.share.bluetooth.BluetoothBasic;
import org.odk.share.bluetooth.BluetoothServer;
import org.odk.share.bluetooth.BluetoothUtils;
import org.odk.share.views.ui.common.injectable.InjectableActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Receive activity, for testing, needs refactor.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class BtReceiverActivity extends InjectableActivity implements BluetoothBasic.Listener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.test_text_view)
    TextView testTextView;

    private BluetoothServer bluetoothServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_receive);
        ButterKnife.bind(this);

        setTitle(getString(R.string.receive_instance_title));
        setSupportActionBar(toolbar);

        bluetoothServer = new BluetoothServer(this);
        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.enableBluetooth();
        }
    }

    @Override
    public void socketNotify(ConnectStatus status, Object obj) {
        if (BluetoothUtils.isActivityDestroyed(this)) {
            return;
        }

        String message;
        switch (status) {
            case CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                message = String.format("connected with device: %s(%s)", dev.getName(), dev.getAddress());
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case DISCONNECTED:
                bluetoothServer.listen();
                message = "lost connection, listen again...";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case DATA:
                message = String.format("\n%s", obj);
                testTextView.append(message);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothServer.detachListener();
        bluetoothServer.close();
    }
}
