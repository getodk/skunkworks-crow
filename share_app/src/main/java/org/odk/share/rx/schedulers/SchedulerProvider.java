package org.odk.share.rx.schedulers;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Shobhit Agarwal
 */

public class SchedulerProvider implements BaseSchedulerProvider {
    private static final Executor backgroundExecutor = Executors.newCachedThreadPool();
    private static final Scheduler BACKGROUND_SCHEDULERS = Schedulers.from(backgroundExecutor);
    private static final Executor internetExecutor = Executors.newCachedThreadPool();
    private static final Scheduler INTERNET_SCHEDULERS = Schedulers.from(internetExecutor);

    @Override
    public Scheduler runOnBackground() {
        return BACKGROUND_SCHEDULERS;
    }

    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @Override
    public Scheduler compute() {
        return Schedulers.computation();
    }

    @Override
    public Scheduler androidThread() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    public Scheduler internet() {
        return INTERNET_SCHEDULERS;
    }
}
