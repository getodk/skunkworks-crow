package org.odk.share.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.odk.share.R;
import org.odk.share.views.ui.bluetooth.BtReceiverActivity;
import org.odk.share.views.ui.bluetooth.BtSenderActivity;
import org.odk.share.views.ui.hotspot.HpReceiverActivity;
import org.odk.share.views.ui.hotspot.HpSenderActivity;
import org.odk.share.views.ui.settings.PreferenceKeys;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 12/07/2019
 */
public class DialogUtils {

    private DialogUtils() {
    }

    /**
     * Create a simple {@link AlertDialog} for showing messages, after that, we finish the {@link Activity}.
     */
    public static AlertDialog createSimpleDialog(@NonNull Context context,
                                                 @NonNull String title,
                                                 @NonNull String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), (dialog, i) -> ((Activity) context).finish());
        return alertDialog;
    }

    /**
     * Create an {@link AlertDialog} for switching receive method.
     */
    public static AlertDialog createMethodSwitchDialog(@NonNull Activity targetActivity, DialogInterface.OnClickListener listener) {
        AlertDialog alertDialog = new AlertDialog.Builder(targetActivity).create();
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, targetActivity.getString(R.string.cancel),
                (dialog, i) -> {
                    dialog.dismiss();
                });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, targetActivity.getString(R.string.switch_method), listener);

        //the icons set in the dialog are for destinations.
        if (targetActivity instanceof BtReceiverActivity || targetActivity instanceof BtSenderActivity) {
            alertDialog.setIcon(R.drawable.ic_wifi_tethering_black_24dp);
            alertDialog.setTitle(targetActivity.getString(R.string.switch_to_hotspot_title));
            alertDialog.setMessage(targetActivity.getString(R.string.hotspot_switch_method));
        } else if (targetActivity instanceof HpReceiverActivity || targetActivity instanceof HpSenderActivity) {
            alertDialog.setIcon(R.drawable.ic_bluetooth_black_24dp);
            alertDialog.setTitle(targetActivity.getString(R.string.switch_to_bluetooth_title));
            alertDialog.setMessage(targetActivity.getString(R.string.bluetooth_switch_method));
        } else {
            throw new IllegalArgumentException("Not a valid activity parameter");
        }

        return alertDialog;
    }

    /**
     * Detecting the default set by user, and using that as main sending method.
     */
    public static void switchToDefaultSendingMethod(@NonNull Context context, @NonNull Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String defaultMethod = prefs.getString(PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD, context.getString(R.string.default_hotspot_ssid));
        if (context.getString(R.string.default_hotspot_ssid).equals(defaultMethod)) {
            intent.setClass(ActivityUtils.getActivity(context), HpSenderActivity.class);
        } else if (context.getString(R.string.bluetooth).equals(defaultMethod)) {
            intent.setClass(ActivityUtils.getActivity(context), BtSenderActivity.class);
        } else {
            throw new IllegalArgumentException("No such default sending method!");
        }

        context.startActivity(intent);
    }

    /**
     * Detecting the default set by user, and using that as main receiving method.
     */
    public static void switchToDefaultReceivingMethod(@NonNull Context context) {
        Intent intent = new Intent();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String defaultMethod = prefs.getString(PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD, context.getString(R.string.default_hotspot_ssid));
        if (context.getString(R.string.default_hotspot_ssid).equals(defaultMethod)) {
            intent.setClass(ActivityUtils.getActivity(context), HpReceiverActivity.class);
        } else if (context.getString(R.string.bluetooth).equals(defaultMethod)) {
            intent.setClass(ActivityUtils.getActivity(context), BtReceiverActivity.class);
        } else {
            throw new IllegalArgumentException("No such default receiving method!");
        }

        context.startActivity(intent);
    }
}
