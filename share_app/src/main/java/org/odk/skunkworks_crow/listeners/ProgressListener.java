package org.odk.skunkworks_crow.listeners;

/**
 * Created by laksh on 5/30/2018.
 */

public interface ProgressListener {

    void uploadingComplete(String result);

    void progressUpdate(int progress, int total);

    void onCancel();
}