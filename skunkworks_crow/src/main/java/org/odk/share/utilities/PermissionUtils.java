package org.odk.share.utilities;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.odk.share.R;

import static android.content.Context.LOCATION_SERVICE;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * For putting utils for permission checks.
 */
public class PermissionUtils {

    private static final String SCHEME = "package";
    public static final int APP_SETTING_REQUEST_CODE = 0x120;

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
     * Showing an alert dialog and send users to app info page.
     */
    public static void showAppInfo(Activity targetActivity, String packageName,
                                   String msg, String deniedMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(targetActivity);
        builder.setMessage(msg);

        builder.setPositiveButton(targetActivity.getString(R.string.permission_open_info_button), (DialogInterface dialog, int which) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
            targetActivity.startActivityForResult(intent, APP_SETTING_REQUEST_CODE);
        });

        builder.setNegativeButton(targetActivity.getString(R.string.cancel), (DialogInterface dialog, int which) -> {
            dialog.dismiss();
            Toast.makeText(targetActivity, deniedMsg, Toast.LENGTH_SHORT).show();
            targetActivity.finish();
        });

        builder.setCancelable(false);
        builder.show();
    }
}


