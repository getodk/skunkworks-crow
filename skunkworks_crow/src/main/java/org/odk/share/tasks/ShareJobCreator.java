package org.odk.share.tasks;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
