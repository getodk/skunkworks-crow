package org.odk.share.services;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.odk.share.events.DownloadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.tasks.DownloadJob;

import java.util.LinkedList;
import java.util.Queue;

import timber.log.Timber;

public class ReceiverService {

    private final Queue<JobRequest> jobs = new LinkedList<>();
    private final RxEventBus rxEventBus;
    private final BaseSchedulerProvider schedulerProvider;

    private JobRequest currentJob;

    public ReceiverService(RxEventBus rxEventBus, BaseSchedulerProvider schedulerProvider) {
        this.rxEventBus = rxEventBus;
        this.schedulerProvider = schedulerProvider;

        addDownloadJobSubscription();
    }

    private void addDownloadJobSubscription() {
        rxEventBus.register(DownloadEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .doOnNext(downloadEvent -> {
                    switch (downloadEvent.getStatus()) {

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

    public void startDownloading(String ip, int port) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString(DownloadJob.IP, ip);
        extras.putInt(DownloadJob.PORT, port);

        // Build request
        JobRequest request = new JobRequest.Builder(DownloadJob.TAG)
                .addExtras(extras)
                .startNow()
                .build();

        if (currentJob != null) {
            jobs.add(request);
        } else {
            startJob(request);
        }

        rxEventBus.post(new DownloadEvent(DownloadEvent.Status.QUEUED));
    }

    private void startJob(JobRequest request) {
        request.schedule();
        Timber.d("Starting download job %d : ", request.getJobId());
        currentJob = request;
    }

    public void cancel() {
        if (currentJob != null) {
            JobManager.instance().getJob(currentJob.getJobId()).cancel();
            rxEventBus.post(new DownloadEvent(DownloadEvent.Status.CANCELLED));
            currentJob = null;
        }
    }
}
