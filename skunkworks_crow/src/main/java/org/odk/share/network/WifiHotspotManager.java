package org.odk.share.network;

import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;

import org.odk.share.preferences.SharedPreferencesHelper;

import javax.inject.Inject;

import static android.content.Context.LOCATION_SERVICE;

public final class WifiHotspotManager {

    @Inject
    Context context;
    @Inject
    SharedPreferencesHelper sharedPreferencesHelper;
    @Inject
    WifiHospotConnector wifiHotspot;

    @Inject
    WifiHotspotManager() {

    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void startHotspot() throws GpsNotEnabledException, TriggerHotspotException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // check if GPS enabled, if not then show location alert dialog to enable
            // show system alert dialog to enable wifi tethering
            if (!isGpsEnabled()) {
                throw new GpsNotEnabledException();
            } else {
                throw new TriggerHotspotException();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // show system alert dialog to enable wifi tethering
            throw new TriggerHotspotException();
        } else {
            // enable wifi tethering
            wifiHotspot.enableHotspot(sharedPreferencesHelper.getHotspotName());
        }
    }

    public void stopHotspot() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            wifiHotspot.disableHotspot();
        }
    }

    // Starting from Android O, wifi hotspot settings can't be read or changed unless
    // the app has special privilege or shared system signature
    // Hence, rely on already saved shared preferences for generating QR Code
    public void saveCurrentSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WifiConfiguration currentConfig = wifiHotspot.getCurrConfig();

            sharedPreferencesHelper.setHotspotName(currentConfig.SSID);
            if (currentConfig.preSharedKey == null) {
                sharedPreferencesHelper.setHotspotPasswordRequired(false);
            } else {
                sharedPreferencesHelper.setHotspotPasswordRequired(true);
                sharedPreferencesHelper.setHotspotPassword(currentConfig.preSharedKey);
            }
        }
    }

    public class GpsNotEnabledException extends Exception {

    }

    public class TriggerHotspotException extends Exception {

    }
}
