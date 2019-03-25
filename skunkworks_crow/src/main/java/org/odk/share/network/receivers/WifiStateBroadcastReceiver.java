package org.odk.share.network.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import org.odk.share.network.listeners.WifiStateListener;

public class WifiStateBroadcastReceiver extends BroadcastReceiver {

    private final Context context;
    private final WifiStateListener listener;
    private boolean isRegistered;

    public WifiStateBroadcastReceiver(Context context, WifiStateListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void register() {
        if (!isRegistered) {
            isRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

            context.getApplicationContext().registerReceiver(this, intentFilter);
        }
    }

    public void unregister() {
        if (isRegistered) {
            context.getApplicationContext().unregisterReceiver(this);
            isRegistered = false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && listener != null) {
            switch (intent.getAction()) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    listener.onStateUpdate(networkInfo.getDetailedState());
                    break;
                case WifiManager.RSSI_CHANGED_ACTION:
                    int rssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1);
                    listener.onRssiChanged(rssi);
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    listener.onScanResultsAvailable();
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                        listener.onWifiStateToggle(false);
                    } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        listener.onWifiStateToggle(true);
                    }
            }
        }
    }
}
