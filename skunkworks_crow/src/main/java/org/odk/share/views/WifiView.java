package org.odk.share.views;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;

import org.odk.share.R;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by laksh on 5/25/2018.
 */

public class WifiView extends AppCompatImageView {
    public WifiView(Context context) {
        super(context);
    }

    public WifiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WifiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateState(boolean isProtected, int rssiLevel) {
        int resId = getResId(isProtected, rssiLevel);
        setImageDrawable(getContext().getResources().getDrawable(resId));
    }

    private int getResId(boolean isProtected, int rssiLevel) {
        if (rssiLevel < -1) {
            rssiLevel = WifiManager.calculateSignalLevel(rssiLevel, 100);
        }

        int resId;
        if (isProtected) {
            if (rssiLevel <= 25) {
                resId = R.drawable.ic_signal_wifilock_1;
            } else if (rssiLevel <= 50) {
                resId = R.drawable.ic_signal_wifilock_2;
            } else if (rssiLevel <= 75) {
                resId = R.drawable.ic_signal_wifilock_3;
            } else {
                resId = R.drawable.ic_signal_wifilock_4;
            }
        } else {
            if (rssiLevel <= 25) {
                resId = R.drawable.ic_signal_wifi_1;
            } else if (rssiLevel <= 50) {
                resId = R.drawable.ic_signal_wifi_2;
            } else if (rssiLevel <= 75) {
                resId = R.drawable.ic_signal_wifi_3;
            } else {
                resId = R.drawable.ic_signal_wifi_4;
            }
        }
        return resId;
    }
}