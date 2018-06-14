package org.odk.share.events;

public class HotspotEvent {

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
}
