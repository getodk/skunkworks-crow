package org.odk.share.rx;


import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import timber.log.Timber;

/**
 * A simple Event Bus powered by Jake Wharton's RxRelay and RxJava2
 *
 * @author Shobhit Agarwal
 */

public class RxEventBus {

    private Relay<Object> busSubject;

    public RxEventBus() {
        busSubject = PublishRelay.create().toSerialized();
    }

    /**
     * Registers for a particular event and returns an observable for subscription.
     *
     * @param eventClass the event
     * @param <T>        the class type of the event
     * @return observable that can be subscribed to.
     */
    public <T> Observable<T> register(@NonNull Class<T> eventClass) {
        return busSubject
                .filter(event -> event.getClass().equals(eventClass))
                .map(obj -> (T) obj);
    }


    /**
     * Sends an event to all the observers who have registered to receive the event type.
     *
     * @param event an Event of any type.
     */
    public void post(@NonNull Object event) {
        Timber.d(event.toString()); // for debugging events that are being sent from place to another
        busSubject.accept(event);
    }

    public Relay<Object> getBusSubject() {
        return busSubject;
    }
}