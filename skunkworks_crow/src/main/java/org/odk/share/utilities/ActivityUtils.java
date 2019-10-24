package org.odk.share.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class ActivityUtils {

    private ActivityUtils() {
    }

    /**
     * Getting the {@link Activity} from {@link Context}.
     *
     * @param context input context
     */
    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            } else {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }

        return null;
    }

    /**
     * launch an {@link Activity}.
     */
    public static void launchActivity(Activity thisActivity,
                                      Class<? extends Activity> targetActivity,
                                      boolean shouldFinish) {
        Intent intent = new Intent(thisActivity, targetActivity);
        thisActivity.startActivity(intent);

        if (shouldFinish) {
            thisActivity.finish();
        }
    }
}
