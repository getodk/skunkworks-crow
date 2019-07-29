package org.odk.share.utilities;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import org.odk.share.R;

import static android.content.Context.LOCATION_SERVICE;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * For putting utils for permission checks.
 */
public class PermissionUtils {

    private PermissionUtils() {
    }

    /**
     * Checking if the location permission has been enabled.
     */
    public static boolean isGPSEnabled(Context targetActivity) {
        LocationManager locationManager = (LocationManager) targetActivity.getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        if (locationManager != null) {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        return gpsEnabled;
    }

    /**
     * Showing an alert dialog for user to enable the location permission from system settings.
     */
    public static void showLocationAlertDialog(Activity targetActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(targetActivity);
        builder.setMessage(R.string.location_settings_dialog);

        builder.setPositiveButton(targetActivity.getString(R.string.settings), (DialogInterface dialog, int which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            targetActivity.startActivity(intent);
        });

        builder.setNegativeButton(targetActivity.getString(R.string.cancel), (DialogInterface dialog, int which) -> {
            dialog.dismiss();
            targetActivity.finish();
        });

        builder.setCancelable(false);
        builder.show();
    }
}


