package org.odk.share.events;

public class BluetoothEvent extends RxEvent {

    private Status status;

    public BluetoothEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        CONNECTED, DISCONNECTED
    }

    @Override
    public String toString() {
        return getClass().getName() + " : " + getStatus();
    }
}
