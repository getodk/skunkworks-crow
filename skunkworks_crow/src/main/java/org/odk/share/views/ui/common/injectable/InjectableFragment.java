package org.odk.share.views.ui.common.injectable;

import dagger.android.support.DaggerFragment;

/**
 * Dependencies of the activity extending {@link InjectableFragment} which are annotated with
 * {@link javax.inject.Inject} will be automatically injected.
 * <p>
 * Those activities should also be registered in {@link org.odk.share.injection.FragmentBuilder}
 */
public abstract class InjectableFragment extends DaggerFragment {

    public InjectableFragment() {

    }
}
