package org.odk.skunkworks_crow.injection.config;

import android.app.Application;

import org.odk.skunkworks_crow.application.Share;
import org.odk.skunkworks_crow.injection.ActivityBuilder;
import org.odk.skunkworks_crow.injection.config.scopes.PerApplication;
import org.odk.skunkworks_crow.services.HotspotService;
import org.odk.skunkworks_crow.tasks.DownloadJob;
import org.odk.skunkworks_crow.tasks.UploadJob;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Primary module, bootstraps the injection system and
 * injects the main Share instance here.
 * <p>
 * Shouldn't be modified unless absolutely necessary.
 */

@PerApplication
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class
})
public interface AppComponent extends AndroidInjector<Share> {

    void inject(Share share);

    void inject(HotspotService hotspotService);

    void inject(UploadJob uploadJob);

    void inject(DownloadJob downloadJob);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
