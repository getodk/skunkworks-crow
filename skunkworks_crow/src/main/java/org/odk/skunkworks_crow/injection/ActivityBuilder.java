package org.odk.skunkworks_crow.injection;

import org.odk.skunkworks_crow.activities.InstancesList;
import org.odk.skunkworks_crow.activities.MainActivity;
import org.odk.skunkworks_crow.activities.SendActivity;
import org.odk.skunkworks_crow.activities.WifiActivity;
import org.odk.skunkworks_crow.injection.config.scopes.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {
    @PerActivity
    @ContributesAndroidInjector()
    abstract InstancesList provideInstancesList();

    @PerActivity
    @ContributesAndroidInjector()
    abstract MainActivity provideMainActivity();

    @PerActivity
    @ContributesAndroidInjector()
    abstract SendActivity provideSendActivity();

    @PerActivity
    @ContributesAndroidInjector()
    abstract WifiActivity provideWifiActivity();
}
