package org.odk.share.application;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by laksh on 5/19/2018.
 */

public class Share extends Application {

    private static Share singleton = null;

    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;
        Timber.plant(new Timber.DebugTree());
    }

    public static Share getInstance() {
        return singleton;
    }
}
