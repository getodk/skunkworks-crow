package org.odk.share.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
     * Detecting the default set by user, and using that as main sending method.
     */
    public static void switchToDefaultSendingMethod(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String defaultMethod = prefs.getString(PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD, context.getString(R.string.hotspot));
        if (context.getString(R.string.hotspot).equals(defaultMethod)) {
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
    public static void switchToDefaultReceivingMethod(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String defaultMethod = prefs.getString(PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD, context.getString(R.string.hotspot));
        if (context.getString(R.string.hotspot).equals(defaultMethod)) {
            intent.setClass(ActivityUtils.getActivity(context), HpReceiverActivity.class);
        } else if (context.getString(R.string.bluetooth).equals(defaultMethod)) {
            intent.setClass(ActivityUtils.getActivity(context), BtReceiverActivity.class);
        } else {
            throw new IllegalArgumentException("No such default receiving method!");
        }

        context.startActivity(intent);
    }
}
