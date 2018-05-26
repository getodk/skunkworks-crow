package org.odk.share.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.controller.WifiHotspotHelper;
import org.odk.share.preferences.SettingsPreference;
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
    private WifiHotspotHelper wifiHotspot;
    private boolean openSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setTitle(getString(R.string.send_forms));
        setSupportActionBar(toolbar);

        wifiHotspot = WifiHotspotHelper.getInstance(this);
        isHotspotRunning = false;
        openSettings = false;

        startHotspot = (Button) findViewById(R.id.bStartHotspot);

        if (wifiHotspot.isHotspotEnabled()) {
            wifiHotspot.disableHotspot();
        }

        startHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMobileDataEnable = WifiHotspotHelper.isMobileDataEnabled(getApplicationContext());
                if (isMobileDataEnable) {
                    // ask user
                    Toast.makeText(getApplicationContext(), "Your mobile data can be consumed. Disable it and then try again",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (!isHotspotRunning) {
                        initiateHotspot();
                    } else {
                        Toast.makeText(MainActivity.this, "Hotspot already running", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
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

        // In devices having Android >= 7, created hotspot having some issues with connecting to other devices.
        // Open settings to trigger the hotspot manually.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            wifiHotspot.saveLastConfig();
            wifiHotspot.setWifiConfig(wifiHotspot.createNewConfig(WifiHotspotHelper.ssid +
                    getString(R.string.hotspot_name_suffix)));
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            openSettings = true;
        } else {
            wifiHotspot.enableHotspot();
            Intent serviceIntent = new Intent(getApplicationContext(), HotspotService.class);
            serviceIntent.setAction(HotspotService.ACTION_START);
            startService(serviceIntent);

            Intent intent = new Intent(getApplicationContext(), HotspotService.class);
            intent.setAction(HotspotService.ACTION_STATUS);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (openSettings) {
            openSettings = false;
            Intent serviceIntent = new Intent(getApplicationContext(), HotspotService.class);
            serviceIntent.setAction(HotspotService.ACTION_START);
            startService(serviceIntent);

            Intent intent = new Intent(getApplicationContext(), HotspotService.class);
            intent.setAction(HotspotService.ACTION_STATUS);
            startService(intent);
        }
        localBroadcastManager.registerReceiver(receiverHotspotEnabled, new IntentFilter(HotspotService.BROADCAST_HOTSPOT_ENABLED));
        localBroadcastManager.registerReceiver(receiverHotspotDisabled, new IntentFilter(HotspotService.BROADCAST_HOTSPOT_DISABLED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        localBroadcastManager.unregisterReceiver(receiverHotspotDisabled);
        localBroadcastManager.unregisterReceiver(receiverHotspotEnabled);
    }

    @Override
    protected void onDestroy() {
        if (isHotspotRunning) {
            wifiHotspot.disableHotspot();
        }
        super.onDestroy();
    }

    private final BroadcastReceiver receiverHotspotDisabled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HotspotService.BROADCAST_HOTSPOT_DISABLED)) {
                isHotspotRunning = false;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsPreference.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver receiverHotspotEnabled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HotspotService.BROADCAST_HOTSPOT_ENABLED)) {
                isHotspotRunning = true;
            }
        }
    };

}