package org.odk.share.views.ui.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.odk.share.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Bluetooth {@link android.app.Activity} for choose different transfer methods.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class BluetoothActivity extends AppCompatActivity {

    @BindView(R.id.btn_receive)
    Button btnReceive;

    @BindView(R.id.btn_send)
    Button btnSend;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ButterKnife.bind(this);
        initEvents();

        setTitle(getString(R.string.bluetooth_act_title));
        setSupportActionBar(toolbar);
    }

    private void initEvents() {
        btnReceive.setOnClickListener((View v) -> {
            startActivity(new Intent(this, BtReceiverActivity.class));
        });

        btnSend.setOnClickListener((View v) -> {
            startActivity(new Intent(this, BtSenderActivity.class));
        });
    }
}
