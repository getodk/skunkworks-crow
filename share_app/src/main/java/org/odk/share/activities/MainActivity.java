package org.odk.share.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.controller.WifiHotspotHelper;
import org.odk.share.services.HotspotService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.bStartHotspot)Button startHotspot;
    @BindView(R.id.bSendForms) Button sendForms;
    @BindView(R.id.bViewWifi) Button viewWifi;

    private boolean isHotspotRunning;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setTitle(getString(R.string.send_forms));
        setSupportActionBar(toolbar);

        isHotspotRunning = false;
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @OnClick (R.id.bViewWifi)
    public void viewWifiNetworks() {
        Intent intent = new Intent(this, WifiActivity.class);
        startActivity(intent);
    }

    @OnClick (R.id.bStartHotspot)
    public void startHotspot() {
        boolean isMobileDataEnable = WifiHotspotHelper.isMobileDataEnabled(getApplicationContext());
        if (isMobileDataEnable) {
            // ask user
            Toast.makeText(this, getString(R.string.mobile_data_message),
                    Toast.LENGTH_LONG).show();
        } else {
            if (!isHotspotRunning) {
                initiateHotspot();
            } else {
                Toast.makeText(this, getString(R.string.hotspot_already_running), Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick (R.id.bSendForms)
    public void selectForms() {
        Intent intent = new Intent(this, InstancesList.class);
        startActivity(intent);
    }

    private void initiateHotspot() {
        Intent serviceIntent = new Intent(getApplicationContext(), HotspotService.class);
        serviceIntent.setAction(HotspotService.ACTION_START);
        startService(serviceIntent);

        Intent intent = new Intent(getApplicationContext(), HotspotService.class);
        intent.setAction(HotspotService.ACTION_STATUS);
        startService(intent);
        isHotspotRunning = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(HotspotService.BROADCAST_HOTSPOT_DISABLED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        if (isHotspotRunning) {
            new WifiHotspotHelper(this).disableHotspot();
        }
        super.onDestroy();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HotspotService.BROADCAST_HOTSPOT_DISABLED)) {
                isHotspotRunning = false;
            }
        }
    };
}