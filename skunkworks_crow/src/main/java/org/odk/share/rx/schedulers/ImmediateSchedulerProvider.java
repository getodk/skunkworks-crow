package org.odk.share.rx.schedulers;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of the {@link BaseSchedulerProvider} making all {@link Scheduler}s execute
 * synchronously so we can easily run assertions in our tests.
 * <p>
 * To achieve this, we are using the {@link io.reactivex.internal.schedulers.TrampolineScheduler} from the {@link Schedulers} class.
 */

public class ImmediateSchedulerProvider implements BaseSchedulerProvider {

    @Override
    public Scheduler runOnBackground() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler io() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler compute() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler androidThread() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler internet() {
        return Schedulers.trampoline();
    }
}

