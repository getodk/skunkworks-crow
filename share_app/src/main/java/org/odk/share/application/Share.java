package org.odk.share.application;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;

import org.odk.share.injection.config.AppComponent;
import org.odk.share.injection.config.DaggerAppComponent;
import org.odk.share.tasks.ShareJobCreator;

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

        try {
            JobManager
                    .create(this)
                    .addJobCreator(new ShareJobCreator());
        } catch (JobManagerCreateException e) {
            Timber.e(e);
        }
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
