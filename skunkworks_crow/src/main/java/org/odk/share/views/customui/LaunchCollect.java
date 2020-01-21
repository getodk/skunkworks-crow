package org.odk.share.views.customui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.odk.share.R;

/**
 * @author by Chromicle (ajayprabhakar369@gmail.com)
 * @since 1/21/2020
 */


public class LaunchCollect {
    private Context context;

    public LaunchCollect(Context context) {
        this.context = context;
    }

    public void openFormInCollect(long instanceId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://org.odk.collect.android.provider.odk.instances/instances/" + instanceId));
        intent.putExtra("formMode", "viewSent");
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, context.getString(R.string.collect_not_installed), Toast.LENGTH_LONG).show();
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}
