package org.odk.share.application;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by laksh on 5/19/2018.
 */

public class Share extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
