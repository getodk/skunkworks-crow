package org.odk.share.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

public final class WifiConnector {

    private final Context context;
    private final WifiManager wifiManager;
    private final WifiBroadcastReceiver wifiBroadcastReceiver;
    private final IntentFilter wifiIntentFilter;

    private boolean isWifiBroadcastRegistered;

    public WifiConnector(Context context, WifiBroadcastReceiver.WifiBroadcastListener listener) {
        this.context = context;

        wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiBroadcastReceiver = new WifiBroadcastReceiver(listener);

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static String unquoted(String name) {
        if (name.startsWith("\"") && name.endsWith("\"")) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    public void registerReceiver() {
        if (!isWifiBroadcastRegistered) {
            context.getApplicationContext().registerReceiver(wifiBroadcastReceiver, wifiIntentFilter);
            isWifiBroadcastRegistered = true;
        }
    }

    public void unregisterReceiver() {
        if (isWifiBroadcastRegistered) {
            context.getApplicationContext().unregisterReceiver(wifiBroadcastReceiver);
            isWifiBroadcastRegistered = false;
        }
    }

    /**
     * Uses the capConnectabilities from ScanResult to determine the security type
     * (https://stackoverflow.com/questions/28905604/android-detecting-if-wifi-is-wep-wpa-wpa2-etc-programmatically)
     * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android-apps/2.0_r1/com/android/settings/wifi/AccessPointState.java#AccessPointState.getScanResultSecurity%28android.net.wifi.ScanResult%29
     */
    public int getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK", "EAP"};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return i;
            }
        }
        return WifiConfiguration.KeyMgmt.NONE;
    }

    /**
     * Gets SSID of Connected WiFi
     *
     * @return Returns the service set identifier (SSID) of the current 802.11 network
     */
    public final String getWifiSSID() {
        String result = null;
        final ConnectivityManager cm =
                (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            final NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if ((connectionInfo != null) && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                    result = connectionInfo.getSSID();
                }
            }
        }
        return result;
    }

    public String getSsid() {
        if (wifiManager.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED) {
            return unquoted(wifiManager.getConnectionInfo().getSSID());
        }
        return "";
    }

    /**
     * Is network available boolean.
     *
     * @return the boolean
     */
    public final boolean isNetworkAvailable() {
        final ConnectivityManager cm = (ConnectivityManager) this.context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            final NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return (netInfo != null) && netInfo.isConnected();
        }
        return false;
    }

    /**
     * Is wifi enabled.
     *
     * @return the boolean
     */
    public final boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }
}