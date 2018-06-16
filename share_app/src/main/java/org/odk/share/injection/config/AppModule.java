package org.odk.share.injection.config;


import android.app.Application;
import android.content.Context;

import org.odk.share.controller.WifiHotspotHelper;
import org.odk.share.injection.config.scopes.PerApplication;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.rx.schedulers.SchedulerProvider;
import org.odk.share.services.ReceiverService;

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

    @Provides
    BaseSchedulerProvider provideSchedulerProvider() {
        return new SchedulerProvider();
    }

    @Provides
    @PerApplication
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    @PerApplication
    ReceiverService provideReceiverService(RxEventBus rxEventBus, BaseSchedulerProvider schedulerProvider) {
        return new ReceiverService(rxEventBus, schedulerProvider);
    }

    @Provides
    @PerApplication
    WifiHotspotHelper provideHotspotHelper(Context context) {
        return new WifiHotspotHelper(context);
    }
}
