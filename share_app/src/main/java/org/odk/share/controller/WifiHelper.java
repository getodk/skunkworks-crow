package org.odk.share.controller;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import org.odk.share.R;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import timber.log.Timber;

/**
 * Created by laksh on 5/22/2018.
 */

public class WifiHelper {

    private final WifiManager wifiManager;
    private Context context;

    public WifiHelper(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    // Checks whether the wifi hotspot is password protected or not
    public static boolean isClose(ScanResult result) {
        return result.capabilities.contains("EAP") || result.capabilities.contains("PSK") || result.capabilities.contains("WEP");
    }

    public void connectToWifi(String ssid, String password) {
        Timber.d("SSID : " + ssid + " " + password);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo.getSSID() != null && wifiInfo.getSSID().equals(ssid)) {
            // already connected to a network
            Toast.makeText(context, context.getString(R.string.already_connected) + " " +
                    ssid, Toast.LENGTH_LONG).show();
        } else {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + ssid + "\"";

            if (password == null) {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            } else {
                conf.preSharedKey = "\"" + password + "\"";
            }
            Timber.d("COnfig " + conf);
            wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            disableOtherNetwork(ssid);
            wifiManager.reconnect();
        }
    }

    private void disableOtherNetwork(String ssid) {
        List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();

        if (wifiConfigurationList == null) {
            return;
        }

        for (WifiConfiguration config : wifiConfigurationList) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.enableNetwork(config.networkId, true);
            } else {
                wifiManager.disableNetwork(config.networkId);
            }
        }
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

    public WifiManager getWifiManager() {
        return wifiManager;
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
