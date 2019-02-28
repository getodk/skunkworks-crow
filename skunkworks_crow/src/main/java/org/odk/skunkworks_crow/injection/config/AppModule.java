package org.odk.skunkworks_crow.injection.config;


import android.app.Application;
import android.content.Context;

import org.odk.skunkworks_crow.controller.WifiHotspotHelper;
import org.odk.skunkworks_crow.injection.config.scopes.PerApplication;
import org.odk.skunkworks_crow.rx.RxEventBus;
import org.odk.skunkworks_crow.rx.schedulers.BaseSchedulerProvider;
import org.odk.skunkworks_crow.rx.schedulers.SchedulerProvider;
import org.odk.skunkworks_crow.services.ReceiverService;
import org.odk.skunkworks_crow.services.SenderService;

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
    SenderService provideSenderService(RxEventBus rxEventBus, BaseSchedulerProvider schedulerProvider) {
        return new SenderService(rxEventBus, schedulerProvider);
    }

    @Provides
    @PerApplication
    WifiHotspotHelper provideHotspotHelper(Context context) {
        return new WifiHotspotHelper(context);
    }
}
