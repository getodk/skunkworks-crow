package org.odk.share.network;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import timber.log.Timber;

public final class WifiConnector {

    private final WifiManager wifiManager;

    public WifiConnector(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private static String unquoted(String name) {
        if (name.startsWith("\"") && name.endsWith("\"")) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    public WifiInfo getActiveConnection() {
        return wifiManager.getConnectionInfo();
    }

    private String quoted(String s) {
        return "\"" + s + "\"";
    }

    public void connectToWifi(int securityType, String ssid, String key) {
        // Check if already connected to that wifi
        String currentSSID = getActiveConnection().getSSID();

        NetworkInfo.DetailedState currentState = WifiInfo.getDetailedStateOf(getActiveConnection().getSupplicantState());
        if (currentState == NetworkInfo.DetailedState.CONNECTED && currentSSID.equals(quoted(ssid))) {
            Timber.d("Already connected");
            return;
        }

        int highestPriorityNumber = 0;
        for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
            if (config.priority > highestPriorityNumber) {
                highestPriorityNumber = config.priority;
            }
        }

        /* Make new connection */
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = quoted(ssid);
        config.priority = highestPriorityNumber + 1;
        config.status = WifiConfiguration.Status.ENABLED;
        if (securityType == WifiConfiguration.KeyMgmt.WPA_PSK) {
            config.preSharedKey = quoted(key);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        Timber.d("Attempting new wifi connection, setting priority number to, connecting %s", config.priority);

        int netId = wifiManager.addNetwork(config);
        if (netId == -1) {
            netId = getExistingNetworkId(config.SSID);
        }

        Timber.d("Network id = %s", netId);

        wifiManager.disconnect(); /* disconnect from whichever wifi you're connected to */
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    private int getExistingNetworkId(String ssid) {
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID.equals(ssid)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
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
    public String getWifiSSID() {
        if (wifiManager.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED) {
            return unquoted(wifiManager.getConnectionInfo().getSSID());
        }
        return "";
    }

    /**
     * Is wifi enabled.
     *
     * @return the boolean
     */
    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public void connect(int networkId) {
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
    }

    public void enableWifi() {
        wifiManager.setWifiEnabled(true);
    }

    private void removeNetworkAndEnableOther(String ssid) {
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
        if (ssid != null) {
            removeNetworkAndEnableOther(ssid);
        }

        wifiManager.setWifiEnabled(false);
    }

    public void startScan() {
        if (!isWifiEnabled()) {
            enableWifi();
        }

        wifiManager.startScan();
    }

    public String getAccessPointIpAddress() {
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        byte[] ipAddress = convertToBytes(dhcpInfo.serverAddress);
        try {
            String ip = InetAddress.getByAddress(ipAddress).getHostAddress();
            return ip.replace("/", "");
        } catch (UnknownHostException e) {
            Timber.e(e);
        }
        return null;
    }

    private byte[] convertToBytes(int hostAddress) {
        return new byte[]{(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
    }
}