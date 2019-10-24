package org.odk.share.events;

public class DownloadEvent extends RxEvent {

    private Status status;
    private String result;
    private int currentProgress;
    private int totalSize;

    public DownloadEvent(Status status) {
        this.status = status;
    }

    public DownloadEvent(Status status, String result) {
        this.status = status;
        this.result = result;
    }

    public DownloadEvent(Status status, int currentProgress, int totalSize) {
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

    public enum Status {
        QUEUED, DOWNLOADING, FINISHED, ERROR, CANCELLED
    }

    @Override
    public String toString() {
        return getClass().getName() + " : " + getStatus() + " : " + getResult();
    }
}
