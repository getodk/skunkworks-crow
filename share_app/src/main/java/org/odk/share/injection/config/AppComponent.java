package org.odk.share.injection.config;

import android.app.Application;

import org.odk.share.application.Share;
import org.odk.share.injection.ActivityBuilder;
import org.odk.share.injection.config.scopes.PerApplication;

import dagger.BindsInstance;
import dagger.Component;
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
public interface AppComponent {

    void inject(Share share);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
