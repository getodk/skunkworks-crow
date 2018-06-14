package org.odk.share.services;

import org.odk.share.rx.RxEventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.CompositeDisposable;

@Singleton
public class ReceiverService {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    RxEventBus rxEventBus;

    @Inject
    ReceiverService() {

    }

}
