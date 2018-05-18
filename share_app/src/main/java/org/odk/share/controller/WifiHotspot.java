package org.odk.share.controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by laksh on 5/17/2018.
 */

public class WifiHotspot {

    Context context;
    Method getWifiApConfig;
    Method setWifiApEnable;
    Method setWifiApConfig;
    Method isWifiApEnabled;
    Method getWifiApState;
    WifiManager wifiManager;
    WifiConfiguration lastConfig;
    WifiConfiguration currConfig;
    private static final String ssid = "ODK-Share";
    private static final String TAG = WifiHotspot.class.getClass().getName();

    public WifiHotspot(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(context.WIFI_SERVICE);
        Method[] methods = wifiManager.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            switch (methods[i].getName()) {
                case "isWifiApEnabled" :
                    isWifiApEnabled = methods[i];
                    break;
                case "setWifiApEnabled":
                    setWifiApEnable = methods[i];
                    break;
                case "getWifiApState":
                    getWifiApState = methods[i];
                    break;
                case "getWifiApConfiguration":
                    getWifiApConfig = methods[i];
                    break;
                case "setWifiApConfiguration":
                    setWifiApConfig = methods[i];
            }
        }
    }

    public boolean isSupported() {
        return isWifiApEnabled != null && setWifiApEnable != null && getWifiApState != null &&
                getWifiApConfig != null && setWifiApConfig != null;
    }

    public WifiConfiguration getWifiConfig() {
        Object obj = null;
        try {
            obj = getWifiApConfig.invoke(wifiManager, null);
            if (obj != null) {
                return (WifiConfiguration) obj;
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public Boolean isHotspotEnabled() {
        Object obj = null;
        try {
            obj = isWifiApEnabled.invoke(wifiManager);
            if (obj == null) {
                return false;
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        }
        return (Boolean) obj;
    }

    public Object setWifiConfig(WifiConfiguration configuration) {
        Object obj = null;
        try {
            obj = setWifiApConfig.invoke(wifiManager, configuration);
            if (obj != null) {
                return obj;
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public boolean enableHotspot() {
        // Save last wifi Configuration
        lastConfig = getWifiConfig();

        // Create New Wifi Configuration
        currConfig = createNewConfig(ssid);
        return toggleHotspot(currConfig, true);
    }

    public boolean disableHotspot() {
        setWifiConfig(lastConfig);
        return toggleHotspot(currConfig, false);
    }

    private boolean toggleHotspot(WifiConfiguration configuration, boolean enable) {
        try {
            Object obj = setWifiApEnable.invoke(wifiManager, configuration, enable);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private WifiConfiguration createNewConfig(String ssid) {
        WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.SSID = ssid;
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(wifiConf);
        wifiManager.saveConfiguration();
        return wifiConf;
    }

    public static boolean isMobileDataEnabled(Context context) {
        boolean enabled = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            enabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return enabled;
    }
}
