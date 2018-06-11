package org.odk.share.application;

import org.odk.share.injection.config.AppComponent;
import org.odk.share.injection.config.DaggerAppComponent;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import timber.log.Timber;

/**
 * Created by laksh on 5/19/2018.
 */

public class Share extends DaggerApplication {

    private static Share singleton = null;
    private AppComponent appComponent;

    public static Share getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;
        Timber.plant(new Timber.DebugTree());
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        appComponent = DaggerAppComponent.builder().application(this).build();
        return appComponent;
    }
}
