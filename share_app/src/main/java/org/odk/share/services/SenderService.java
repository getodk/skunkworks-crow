package org.odk.share.services;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.odk.share.events.UploadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.tasks.UploadJob;

import java.util.LinkedList;
import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class SenderService {

    private final Queue<JobRequest> jobs = new LinkedList<>();
    private final RxEventBus rxEventBus;
    private final BaseSchedulerProvider schedulerProvider;
    private JobRequest currentJob;

    @Inject
    public SenderService(RxEventBus rxEventBus, BaseSchedulerProvider schedulerProvider) {
        this.rxEventBus = rxEventBus;
        this.schedulerProvider = schedulerProvider;

        addUploadJobSubscription();
    }

    private void addUploadJobSubscription() {
        rxEventBus.register(UploadEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .doOnNext(uploadEvent -> {
                    switch (uploadEvent.getStatus()) {

                        case CANCELLED:
                        case ERROR:
                            jobs.clear();
                            currentJob = null;
                            break;

                        case FINISHED:
                            if (jobs.size() > 0) {
                                startJob(jobs.remove());
                            } else {
                                currentJob = null;
                            }
                            break;
                    }
                }).subscribe();

    }

    public void startUploading(long[] instancesToSend, int port) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putLongArray(UploadJob.INSTANCES, instancesToSend);
        extras.putInt(UploadJob.PORT, port);

        // Build request
        JobRequest request = new JobRequest.Builder(UploadJob.TAG)
                .addExtras(extras)
                .startNow()
                .build();

        if (currentJob != null) {
            jobs.add(request);
        } else {
            startJob(request);
        }

        rxEventBus.post(new UploadEvent(UploadEvent.Status.QUEUED));
    }

    private void startJob(JobRequest request) {
        request.schedule();
        Timber.d("Starting upload job %d : ", request.getJobId());
        currentJob = request;
    }

    public void cancel() {
        if (currentJob != null) {
            JobManager.instance().getJob(currentJob.getJobId()).cancel();
            rxEventBus.post(new UploadEvent(UploadEvent.Status.CANCELLED));
            currentJob = null;
        }
    }
}
