package org.odk.share.rx.schedulers;

import io.reactivex.Scheduler;


/**
 * Allow providing different types of {@link Scheduler}s.
 */

public interface BaseSchedulerProvider {

    Scheduler runOnBackground();

    Scheduler io();

    Scheduler compute();

    Scheduler androidThread();

    Scheduler internet();
}

