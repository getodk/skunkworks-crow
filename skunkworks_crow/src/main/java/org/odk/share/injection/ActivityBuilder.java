package org.odk.share.injection;

import org.odk.share.views.ui.bluetooth.BtReceiverActivity;
import org.odk.share.views.ui.bluetooth.BtSenderActivity;
import org.odk.share.views.ui.instance.InstanceManagerTabs;
import org.odk.share.views.ui.instance.InstancesList;
import org.odk.share.views.ui.main.MainActivity;
import org.odk.share.views.ui.hotspot.HpReceiverActivity;
import org.odk.share.views.ui.review.ReviewFormActivity;
import org.odk.share.views.ui.hotspot.HpSenderActivity;
import org.odk.share.views.ui.send.SendFormsActivity;
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
    abstract HpSenderActivity provideSendActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract HpReceiverActivity provideWifiActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract ReviewFormActivity provideReviewFormActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract InstanceManagerTabs provideInstanceManagerTabs();

    @PerActivity
    @ContributesAndroidInjector
    abstract SendFormsActivity provideSendFormsActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract BtReceiverActivity provideBtReceiverActivity();

    @PerActivity
    @ContributesAndroidInjector
    abstract BtSenderActivity provideBtSenderActivity();
}
