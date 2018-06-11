package org.odk.share.injection.config;


import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * Add Application level providers here, i.e. if you want to
 * inject something into the Share instance.
 */
@Module
class AppModule {

    //expose Application as an injectable context

    @Provides
    Context bindContext(Application application) {
        return application;
    }

}
