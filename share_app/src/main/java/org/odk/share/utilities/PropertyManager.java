/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.share.utilities;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Used to return JavaRosa type device properties
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Jason Rogena (jrogena@ona.io)
 */

public class PropertyManager {

    public static final String ANDROID6_FAKE_MAC = "02:00:00:00:00:00";

    private String t = "PropertyManager";


    private HashMap<String, String> mProperties;

    public final static String DEVICE_ID_PROPERTY = "deviceid"; // imei
    public final static String SUBSCRIBER_ID_PROPERTY = "subscriberid"; // imsi
    public final static String SIM_SERIAL_PROPERTY = "simserial";
    public final static String PHONE_NUMBER_PROPERTY = "phonenumber";

    public PropertyManager(Context context) {
        TelephonyManager mTelephonyManager;
        Log.i(t, "calling constructor");

        Context mContext = context;

        mProperties = new HashMap<>();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = mTelephonyManager.getDeviceId();
        if (deviceId != null && (deviceId.contains("*") || deviceId.contains("000000000000000"))) {
            deviceId =
                    Settings.Secure
                            .getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        }

        if (deviceId == null) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null && !ANDROID6_FAKE_MAC.equals(info.getMacAddress())) {
                deviceId = info.getMacAddress();
            }
        }

        // if it is still null, use ANDROID_ID
        if (deviceId == null) {
            deviceId =
                    Settings.Secure
                            .getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        mProperties.put(DEVICE_ID_PROPERTY, deviceId);

        String value;

        value = mTelephonyManager.getSubscriberId();
        if (value != null) {
            mProperties.put(SUBSCRIBER_ID_PROPERTY, value);
        }
        value = mTelephonyManager.getSimSerialNumber();
        if (value != null) {
            mProperties.put(SIM_SERIAL_PROPERTY, value);
        }
        value = mTelephonyManager.getLine1Number();
        if (value != null) {
            mProperties.put(PHONE_NUMBER_PROPERTY, value);
        }
    }

    public String getSingularProperty(String propertyName) {
        // for now, all property names are in english...
        return mProperties.get(propertyName.toLowerCase(Locale.ENGLISH));
    }

}