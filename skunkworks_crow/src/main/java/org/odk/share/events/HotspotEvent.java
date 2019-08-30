package org.odk.share.events;

public class HotspotEvent extends RxEvent {

    private Status status;

    public HotspotEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        ENABLED, DISABLED
    }

    @Override
    public String toString() {
        return getClass().getName() + " : " + getStatus();
    }
}
