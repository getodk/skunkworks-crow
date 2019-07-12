package org.odk.share.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import org.odk.share.R;
import org.odk.share.views.ui.bluetooth.BtReceiverActivity;
import org.odk.share.views.ui.bluetooth.BtSenderActivity;
import org.odk.share.views.ui.hotspot.HpReceiverActivity;
import org.odk.share.views.ui.hotspot.HpSenderActivity;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 12/07/2019
 */
public class DialogUtils {

    private DialogUtils() {
    }

    /**
     * Show a {@link AlertDialog} for choosing a sender methods.
     */
    public static AlertDialog showSenderMethodsDialog(Context context, Intent intent, String title) {
        String[] options = {context.getString(R.string.method_bluetooth), context.getString(R.string.method_wifi_hotspot)};
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(R.drawable.ic_help_outline)
                .setItems(options, (DialogInterface dialog, int which) -> {
                    if (ActivityUtils.getActivity(context) != null) {
                        switch (which) {
                            case 0:
                                intent.setClass(ActivityUtils.getActivity(context), BtSenderActivity.class);
                                break;
                            case 1:
                                intent.setClass(ActivityUtils.getActivity(context), HpSenderActivity.class);
                                break;
                        }
                        context.startActivity(intent);
                    }
                }).create();
    }

    /**
     * Show a {@link AlertDialog} for choosing a sender methods.
     */
    public static AlertDialog showReceiverMethodsDialog(Context context, Intent intent, String title) {
        String[] options = {context.getString(R.string.method_bluetooth), context.getString(R.string.method_wifi_hotspot)};
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(R.drawable.ic_help_outline)
                .setItems(options, (DialogInterface dialog, int which) -> {
                    if (ActivityUtils.getActivity(context) != null) {
                        switch (which) {
                            case 0:
                                intent.setClass(ActivityUtils.getActivity(context), BtReceiverActivity.class);
                                break;
                            case 1:
                                intent.setClass(ActivityUtils.getActivity(context), HpReceiverActivity.class);
                                break;
                        }
                        context.startActivity(intent);
                    }
                }).create();
    }
}
