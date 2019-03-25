package org.odk.share.network.listeners;

import android.net.NetworkInfo;

public interface WifiStateListener {

    void onStateUpdate(NetworkInfo.DetailedState detailedState);

    void onRssiChanged(int rssi);

    void onScanResultsAvailable();

    void onWifiStateToggle(boolean isEnabled);
}
