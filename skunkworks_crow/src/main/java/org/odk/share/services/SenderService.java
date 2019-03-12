package org.odk.share.services;

import org.odk.share.events.UploadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.tasks.UploadJob;

import java.util.LinkedList;
import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import timber.log.Timber;

import static org.odk.share.fragments.ReviewedInstancesFragment.MODE;

@Singleton
public class SenderService {

    private final Queue<WorkRequest> jobs = new LinkedList<>();
    private final RxEventBus rxEventBus;
    private final BaseSchedulerProvider schedulerProvider;
    private WorkRequest currentJob;
    private WorkManager workManager = WorkManager.getInstance();

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
        Data extras = new Data.Builder()
                .putLongArray(UploadJob.INSTANCES, instancesToSend)
                .putInt(UploadJob.PORT, port)
                .putInt(MODE, mode)
                .build();

        // Build request
        WorkRequest request = new OneTimeWorkRequest.Builder(UploadJob.class)
                .setInputData(extras)
                .build();

        if (currentJob != null) {
            jobs.add(request);
        } else {
            startJob(request);
        }
    }

    private void startJob(WorkRequest request) {
        workManager.enqueue(request);
        Timber.d("Starting upload job %s : ", request.getId().toString());
        currentJob = request;
    }

    public void cancel() {
        if (currentJob != null) {
                workManager.cancelWorkById(currentJob.getId());
                rxEventBus.post(new UploadEvent(UploadEvent.Status.CANCELLED));
            } else {
                Timber.e("Pending job not found : %s", currentJob);
            }
            currentJob = null;
        }
    }

