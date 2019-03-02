package org.odk.share.injection.config;

import android.app.Application;

import org.odk.share.application.Share;
import org.odk.share.injection.ActivityBuilder;
import org.odk.share.injection.FragmentBuilder;
import org.odk.share.injection.config.scopes.PerApplication;
import org.odk.share.services.HotspotService;
import org.odk.share.tasks.DownloadJob;
import org.odk.share.tasks.UploadJob;

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
        ActivityBuilder.class,
        FragmentBuilder.class
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
