package org.odk.share.views.ui.common.injectable;

import android.annotation.SuppressLint;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * Dependencies of the activity extending {@link InjectableActivity} which are annotated with
 * {@link javax.inject.Inject} will be automatically injected.
 * <p>
 * Those activities should also be registered in {@link org.odk.share.injection.ActivityBuilder}
 */
@SuppressLint("Registered")
public class InjectableActivity extends DaggerAppCompatActivity {
}
