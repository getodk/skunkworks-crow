package org.odk.skunkworks_crow.activities;

import android.annotation.SuppressLint;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * Dependencies of the activity extending {@link InjectableActivity} which are annotated with
 * {@link javax.inject.Inject} will be automatically injected.
 * <p>
 * Those activities should also be registered in {@link org.odk.skunkworks_crow.injection.ActivityBuilder}
 */
@SuppressLint("Registered")
public class InjectableActivity extends DaggerAppCompatActivity {
}
