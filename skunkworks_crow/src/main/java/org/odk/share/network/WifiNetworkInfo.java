package org.odk.share.network;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;

import java.io.Serializable;

public class WifiNetworkInfo implements Serializable {

    private static final String DEFAULT_SSID = "\"<unknown ssid>\"";

    private NetworkInfo.DetailedState state = NetworkInfo.DetailedState.DISCONNECTED;
    private int securityType = WifiConfiguration.KeyMgmt.WPA_PSK;
    private String ssid = DEFAULT_SSID;
    private int rssi = -1;
    private String ip = "0.0.0.0";
    private boolean untrusted;
    private int netId;

    @Override
    public String toString() {
        return ssid + " " + rssi + " " + ip + " " + untrusted + " " + netId;
    }

    public int getNetId() {
        return netId;
    }

    public void setNetId(int netId) {
        this.netId = netId;
    }

    public boolean isUntrusted() {
        return untrusted;
    }

    public void setUntrusted(boolean untrusted) {
        this.untrusted = untrusted;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

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