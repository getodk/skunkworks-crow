package org.odk.share.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.controller.WifiHotspot;
import org.odk.share.services.HotspotService;

public class MainActivity extends AppCompatActivity {

    private Button startHotspot;
    private boolean isHotspotRunning;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isHotspotRunning = false;
        startHotspot = (Button) findViewById(R.id.bStartHotspot);
        startHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMobileDataEnable = WifiHotspot.isMobileDataEnabled(getApplicationContext());
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
            new WifiHotspot(this).disableHotspot();
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