package org.odk.share.controller;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import org.odk.share.R;

import java.util.List;

/**
 * Created by laksh on 5/22/2018.
 */

public class WifiHelper {

    private final WifiManager wifiManager;
    public static WifiHelper wifiHelper;
    private Context context;

    private WifiHelper(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static synchronized WifiHelper getInstance(Context context) {
                if (wifiHelper == null) {
            wifiHelper = new WifiHelper(context);
        }
        return wifiHelper;
    }

    public void connectToWifi(ScanResult wifiNetwork, String password) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo.getBSSID() != null && wifiInfo.getBSSID().equals(wifiNetwork.BSSID)) {
            // already connected to a network
            Toast.makeText(context, context.getString(R.string.already_connected) + " " +
                    wifiNetwork.SSID, Toast.LENGTH_LONG).show();
        } else {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + wifiNetwork.SSID + "\"";

            if (WifiHelper.isClose(wifiNetwork)) {
                conf.preSharedKey = "\"" + password + "\"";
            } else {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }

            wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            disableOtherNetwork(wifiNetwork.SSID);
            wifiManager.reconnect();
        }
    }

    public void disableOtherNetwork(String ssid) {
        List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : wifiConfigurationList) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.enableNetwork(config.networkId, true);
            } else {
                wifiManager.disableNetwork(config.networkId);
            }
        }
    }

    public void removeNetworkAndEnableOther(String ssid) {
        List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : wifiConfigurationList) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disableNetwork(config.networkId);
                wifiManager.removeNetwork(config.networkId);
            } else {
                wifiManager.disableNetwork(config.networkId);
            }
        }
        wifiManager.saveConfiguration();
        wifiManager.reconnect();
    }

    public void disableWifi(String ssid) {
        removeNetworkAndEnableOther(ssid);
        wifiManager.setWifiEnabled(false);
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
