package org.odk.skunkworks_crow.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class ShareJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case UploadJob.TAG:
                return new UploadJob();
            case DownloadJob.TAG:
                return new DownloadJob();
            default:
                return null;
        }
    }
}
