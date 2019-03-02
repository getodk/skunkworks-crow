package org.odk.share.injection;

import org.odk.share.activities.InstanceManagerTabs;
import org.odk.share.activities.InstancesList;
import org.odk.share.activities.MainActivity;
import org.odk.share.activities.ReviewFormActivity;
import org.odk.share.activities.SendActivity;
import org.odk.share.activities.SendFormsActivity;
import org.odk.share.activities.WifiActivity;
import org.odk.share.injection.config.scopes.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {
    @PerActivity
    @ContributesAndroidInjector
    abstract InstancesList provideInstancesList();

    @PerActivity
    @ContributesAndroidInjector
    abstract MainActivity provideMainActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract SendActivity provideSendActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract WifiActivity provideWifiActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract ReviewFormActivity provideReviewFormActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract InstanceManagerTabs provideInstanceManagerTabs();

    @PerActivity
    @ContributesAndroidInjector
    abstract SendFormsActivity provideSendFormsActivity();
}
