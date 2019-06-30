package org.odk.share.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import java.util.UUID;

/**
 * Basic tools for bluetooth features.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 04/06/2019
 */
public class BluetoothUtils {

    // UUID for security bluetooth pairing.
    public static final UUID SPP_UUID = UUID.fromString("724f00a0-795e-4272-a85a-11075e760e58");

    private BluetoothUtils() {

    }

    private static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Checking if current device supports bluetooth.
     */
    public static boolean isSupportBluetooth() {
        if (getBluetoothAdapter() != null) {
            return true;
        } else {
            throw new IllegalStateException("Your device does not support bluetooth!");
        }
    }

    /**
     * Checking if bluetooth is enabled.
     */
    public static boolean isBluetoothEnabled() {
        return isSupportBluetooth() && getBluetoothAdapter().isEnabled();
    }

    /**
     * Disable and enable the bluetooth.
     */
    public static void disableBluetooth() {
        if (isBluetoothEnabled()) {
            new Thread(() -> {
                getBluetoothAdapter().disable();
            }).start();
        }
    }

    public static void enableBluetooth() {
        if (!isBluetoothEnabled()) {
            new Thread(() -> {
                getBluetoothAdapter().enable();
            }).start();
        }
    }

    /**
     * Checking if the current {@link android.app.Activity} is destroyed.
     */
    public static boolean isActivityDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed();
        } else {
            return activity.isChangingConfigurations() || activity.isFinishing();
        }
    }

}
