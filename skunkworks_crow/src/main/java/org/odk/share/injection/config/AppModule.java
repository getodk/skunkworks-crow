package org.odk.share.injection.config;


import android.app.Application;
import android.content.Context;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.share.dao.InstanceMapDao;
import org.odk.share.dao.TransferDao;
import org.odk.share.injection.config.scopes.PerApplication;
import org.odk.share.network.WifiHospotConnector;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.rx.schedulers.SchedulerProvider;
import org.odk.share.services.ReceiverService;
import org.odk.share.services.SenderService;

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
    WifiHospotConnector provideHotspotHelper(Context context) {
        return new WifiHospotConnector(context);
    }

    @Provides
    TransferDao provideTransferDao(Context context) {
        return new TransferDao(context);
    }

    @Provides
    FormsDao provideFormsDao(Context context) {
        return new FormsDao(context);
    }

    @Provides
    InstancesDao provideInstancesDao(Context context) {
        return new InstancesDao(context);
    }

    @Provides
    InstanceMapDao provideInstanceMapDao(Context context) {
        return new InstanceMapDao(context);
    }
}
