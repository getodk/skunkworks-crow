package org.odk.share.tasks;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.odk.share.application.Share;
import org.odk.share.rx.RxEventBus;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class UploadJob extends Job {

    public static final String TAG = "formUploadJob";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    RxEventBus rxEventBus;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        ((Share) getContext().getApplicationContext()).getAppComponent().inject(this);

        return null;
    }
}
