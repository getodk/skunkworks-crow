package org.odk.share.injection;

import org.odk.share.fragments.BlankFormsFragment;
import org.odk.share.fragments.FilledFormsFragment;
import org.odk.share.fragments.ReceivedInstancesFragment;
import org.odk.share.fragments.ReviewedInstancesFragment;
import org.odk.share.fragments.SentInstancesFragment;
import org.odk.share.fragments.StatisticsFragment;
import org.odk.share.injection.config.scopes.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuilder {

    @PerActivity
    @ContributesAndroidInjector()
    abstract StatisticsFragment provideStatisticsFragment();


    @PerActivity
    @ContributesAndroidInjector()
    abstract SentInstancesFragment provideSentInstancesFragment();

    @PerActivity
    @ContributesAndroidInjector()
    abstract ReceivedInstancesFragment provideReceivedInstancesFragment();

    @PerActivity
    @ContributesAndroidInjector()
    abstract ReviewedInstancesFragment provideReviewedInstancesFragment();

    @PerActivity
    @ContributesAndroidInjector()
    abstract BlankFormsFragment provideBlankFormsFragment();

    @PerActivity
    @ContributesAndroidInjector()
    abstract FilledFormsFragment provideFilledFormsFragment();
}
