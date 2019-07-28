package org.odk.share.utilities;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.odk.share.R;

import static android.content.Context.LOCATION_SERVICE;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * For putting utils for permission checks.
 */
public class PermissionUtils {

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 0x110;
    private static final int PERMISSIONS_REQUEST_COARSE_LOCATION = 0x111;

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

    /**
     * Request for the location permission.
     */
    public static void requestLocationPermission(Activity activity) { 
        // ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
            ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_COARSE_LOCATION);
            ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }
}


