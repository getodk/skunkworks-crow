package org.odk.share.bluetooth;

import android.os.Handler;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 04/06/2019
 */
public class BluetoothUtils {

    private static final String TAG = BluetoothUtils.class.getSimpleName();
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler sHandler = new Handler();

    public static void mkdirs(String filePath) {
        boolean mk = new File(filePath).mkdirs();
        Timber.d(TAG + "mkdirs: " + mk);
    }

    public static void runOnUi(Runnable runnable) {
        sHandler.post(runnable);
    }
}
