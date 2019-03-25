package org.odk.share.network;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;

public class WifiNetworkInfo {

    private NetworkInfo.DetailedState state = NetworkInfo.DetailedState.DISCONNECTED;
    private int securityType = WifiConfiguration.KeyMgmt.WPA_PSK;
    private String ssid = "unknown";
    private int rssi = -1;

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getSecurityType() {
        return securityType;
    }

    public void setSecurityType(int securityType) {
        this.securityType = securityType;
    }

    public NetworkInfo.DetailedState getState() {
        return state;
    }

    public void setState(NetworkInfo.DetailedState state) {
        this.state = state;
    }
}