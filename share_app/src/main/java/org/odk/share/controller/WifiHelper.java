package org.odk.share.controller;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

/**
 * Created by laksh on 5/22/2018.
 */

public class WifiHelper {

    private final WifiManager wifiManager;

    public WifiHelper(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    // Checks whether the wifi hotspot is password protected or not
    public static boolean isClose(ScanResult result) {
        if (result.capabilities.contains("EAP") || result.capabilities.contains("PSK") || result.capabilities.contains("WEP")) {
            return true;
        }
        return false;
    }
}
