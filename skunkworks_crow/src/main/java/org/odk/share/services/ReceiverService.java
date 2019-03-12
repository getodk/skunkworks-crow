package org.odk.share.services;

import org.odk.share.events.DownloadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.tasks.DownloadJob;

import java.util.LinkedList;
import java.util.Queue;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import timber.log.Timber;

public class ReceiverService {

    private final Queue<WorkRequest> jobs = new LinkedList<>();
    private final RxEventBus rxEventBus;
    private final BaseSchedulerProvider schedulerProvider;

    private WorkRequest currentJob;
    private WorkManager workManager = WorkManager.getInstance();

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
        Data extras = new Data.Builder()
                .putString(DownloadJob.IP, ip)
                .putInt(DownloadJob.PORT, port)
                .build();

        // Build request
        WorkRequest request = new OneTimeWorkRequest.Builder(DownloadJob.class)
                .setInputData(extras)
                .build();

        if (currentJob != null) {
            jobs.add(request);
        } else {
            startJob(request);
        }

        rxEventBus.post(new DownloadEvent(DownloadEvent.Status.QUEUED));
    }

    private void startJob(WorkRequest request) {
        workManager.enqueue(request);
        Timber.d("Starting download job %s : ", request.getId());
        currentJob = request;
    }

    public void cancel() {
        if (currentJob != null) {
                workManager.cancelWorkById(currentJob.getId());
                rxEventBus.post(new DownloadEvent(DownloadEvent.Status.CANCELLED));
            } else {
                Timber.e("Pending job not found : %s", currentJob);
            }
            currentJob = null;
    }

}
