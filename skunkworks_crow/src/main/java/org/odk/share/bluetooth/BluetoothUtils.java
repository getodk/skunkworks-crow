package org.odk.share.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Handler;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * Basic tools for bluetooth features.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 04/06/2019
 */
public class BluetoothUtils {

    private BluetoothUtils() {

    }

    private static final String TAG = BluetoothUtils.class.getSimpleName();
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler sHandler = new Handler();


    /**
     * TODO: if the odk share has familiar methods, please replace this.
     */
    public static void mkdirs(String filePath) {
        boolean mk = new File(filePath).mkdirs();
        Timber.d(TAG + "mkdirs: " + mk);
    }

    /**
     * Run on main thread.
     */
    public static void runOnUi(Runnable runnable) {
        sHandler.post(runnable);
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
