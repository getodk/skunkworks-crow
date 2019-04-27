package org.odk.share.events;

public class UploadEvent extends RxEvent {

    private Status status;
    private String result;
    private int currentProgress;
    private int totalSize;

    public UploadEvent(Status status) {
        this.status = status;
    }

    public UploadEvent(Status status, String result) {
        this.status = status;
        this.result = result;
    }

    public UploadEvent(Status status, int currentProgress, int totalSize) {
        this.status = status;
        this.currentProgress = currentProgress;
        this.totalSize = totalSize;
    }

    public Status getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getTotalSize() {
        return totalSize;
    }

    @Override
    public String toString() {
        return getClass().getName() + " : " + getStatus() + " : " + getResult();
    }

    public enum Status {
        QUEUED, UPLOADING, FINISHED, ERROR, CANCELLED
    }

}
