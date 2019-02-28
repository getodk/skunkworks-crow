package org.odk.skunkworks_crow.services;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.odk.skunkworks_crow.events.UploadEvent;
import org.odk.skunkworks_crow.rx.RxEventBus;
import org.odk.skunkworks_crow.rx.schedulers.BaseSchedulerProvider;
import org.odk.skunkworks_crow.tasks.UploadJob;

import java.util.LinkedList;
import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

import static org.odk.skunkworks_crow.fragments.ReviewedInstancesFragment.MODE;

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

    public void startUploading(long[] instancesToSend, int port, int mode) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putLongArray(UploadJob.INSTANCES, instancesToSend);
        extras.putInt(UploadJob.PORT, port);
        extras.putInt(MODE, mode);

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
    }

    private void startJob(JobRequest request) {
        request.schedule();
        Timber.d("Starting upload job %d : ", request.getJobId());
        currentJob = request;
    }

    public void cancel() {
        if (currentJob != null) {
            Job job = JobManager.instance().getJob(currentJob.getJobId());
            if (job != null) {
                job.cancel();
                rxEventBus.post(new UploadEvent(UploadEvent.Status.CANCELLED));
            } else {
                Timber.e("Pending job not found : %s", currentJob);
            }
            currentJob = null;
        }
    }
}
