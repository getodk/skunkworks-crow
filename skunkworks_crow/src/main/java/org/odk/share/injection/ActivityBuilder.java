package org.odk.share.injection;

import org.odk.share.views.ui.instance.InstanceManagerTabs;
import org.odk.share.views.ui.instance.InstancesList;
import org.odk.share.views.ui.main.MainActivity;
import org.odk.share.views.ui.review.ReviewFormActivity;
import org.odk.share.views.ui.send.SendActivity;
import org.odk.share.views.ui.send.SendFormsActivity;
import org.odk.share.views.ui.receive.WifiActivity;
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
