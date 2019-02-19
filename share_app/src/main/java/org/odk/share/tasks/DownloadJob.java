package org.odk.share.tasks;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.odk.share.R;
import org.odk.share.application.Share;
import org.odk.share.dao.FormsDao;
import org.odk.share.dao.InstanceMapDao;
import org.odk.share.dao.InstancesDao;
import org.odk.share.dao.TransferDao;
import org.odk.share.database.ShareDatabaseHelper;
import org.odk.share.dto.TransferInstance;
import org.odk.share.events.DownloadEvent;
import org.odk.share.preferences.PreferenceKeys;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.provider.InstanceProviderAPI;
import org.odk.share.rx.RxEventBus;
import org.odk.share.utilities.ApplicationConstants;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.share.application.Share.FORMS_DIR_NAME;
import static org.odk.share.application.Share.INSTANCES_DIR_NAME;
import static org.odk.share.dto.InstanceMap.INSTANCE_UUID;
import static org.odk.share.dto.TransferInstance.INSTANCE_ID;
import static org.odk.share.dto.TransferInstance.INSTRUCTIONS;
import static org.odk.share.dto.TransferInstance.RECEIVED_REVIEW_STATUS;
import static org.odk.share.dto.TransferInstance.STATUS_FORM_RECEIVE;
import static org.odk.share.dto.TransferInstance.TRANSFER_STATUS;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.SUBMISSION_URI;
import static org.odk.share.utilities.ApplicationConstants.SEND_FILL_FORM_MODE;

public class DownloadJob extends Job {

    public static final String TAG = "formDownloadJob";
    public static final String IP = "ip";
    public static final String PORT = "port";
    private static final int TIMEOUT = 2000;

    @Inject
    RxEventBus rxEventBus;

    private String ip;
    private int port;
    private int total;
    private int progress;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private StringBuilder sbResult;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        ((Share) getContext().getApplicationContext()).getAppComponent().inject(this);

        initJob(params);
        rxEventBus.post(receiveForms());

        return null;
    }

    private void initJob(Params params) {
        ip = params.getExtras().getString(IP, "");
        port = params.getExtras().getInt(PORT, -1);
        sbResult = new StringBuilder();
    }

    private DownloadEvent receiveForms() {
        Timber.d("Socket " + ip + " " + port);

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
            Timber.d("Socket connected");
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(socket.getOutputStream());
            int mode = dis.readInt();
            if (mode == SEND_FILL_FORM_MODE) {
                total = dis.readInt();
                int num = dis.readInt();
                Timber.d("Number of forms : %d", num);
                for (int i = 0; i < num; i++) {
                    Timber.d("Downloading form : %d", i + 1);
                    boolean result = readFormAndInstances();
                    Timber.d("Form %d downloaded = %s", i + 1, result);
                }
            } else {
                total = dis.readInt();
                for (int i = 0; i < total; i++) {
                    Timber.d("Downloading blank form: %d", i + 1);
                    readBlankForm();
                    Timber.d("Downloaded blank form %d", i + 1);
                }
            }

            // close connection
            socket.close();
            dos.close();
            dis.close();

        } catch (IOException | IllegalArgumentException e) {
            Timber.e(e);
            return new DownloadEvent(DownloadEvent.Status.ERROR, e.getMessage());
        }

        return new DownloadEvent(DownloadEvent.Status.FINISHED, sbResult.toString());
    }

    @Override
    protected void onCancel() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (dos != null) {
                dos.close();
            }
            if (dis != null) {
                dis.close();
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private boolean readFormAndInstances() {
        try {
            Timber.d("readFormAndInstances");
            String formId = dis.readUTF();
            String formVersion = dis.readUTF();
            Timber.d(formId + " " + formVersion);
            if (formVersion.equals("-1")) {
                formVersion = null;
            }

            boolean formExists = isFormExits(formId, formVersion);
            Timber.d("Form exists %s", formExists);

            dos.writeBoolean(formExists);

            if (!formExists) {
                // read form
                readForm();
            }

            // readInstances
            readInstances(formId, formVersion);
            return true;
        } catch (IOException e) {
            Timber.e(e);
        }
        return false;
    }

    private void readBlankForm() {
        String formId = null;
        try {
            Timber.d("Reading blank form");
            formId = dis.readUTF();
            String formVersion = dis.readUTF();
            Timber.d(formId + " " + formVersion);
            if (formVersion.equals("-1")) {
                formVersion = null;
            }

            boolean formExists = isFormExits(formId, formVersion);
            Timber.d("Form exists %s", formExists);

            dos.writeBoolean(formExists);

            if (!formExists) {
                // read form
                readForm();
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private boolean isFormExits(String formId, String formVersion) {
        String[] selectionArgs;
        String selection;

        if (formVersion == null) {
            selectionArgs = new String[]{formId};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + " IS NULL";
        } else {
            selectionArgs = new String[]{formId, formVersion};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + "=?";
        }

        Cursor cursor = new FormsDao().getFormsCursor(null, selection, selectionArgs, null);

        return cursor != null && cursor.getCount() > 0;
    }

    private void readForm() {
        try {
            String displayName = dis.readUTF();
            String formId = dis.readUTF();
            String formVersion = dis.readUTF();
            String submissionUri = dis.readUTF();

            if (formVersion.equals("-1")) {
                formVersion = null;
            }

            if (submissionUri.equals("-1")) {
                submissionUri = null;
            }

            Timber.d(displayName + " " + formId + " " + formVersion + " " + submissionUri);
            String formName = receiveFile(getFormsPath());
            int numOfRes = dis.readInt();
            String formMediaPath = getFormsPath() + File.separator + displayName + "-media";
            while (numOfRes-- > 0) {
                receiveFile(formMediaPath);
            }

            // Add row in forms db
            ContentValues values = new ContentValues();
            values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, getFormsPath() + File.separator + formName);
            values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, displayName);
            values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, formId);
            values.put(FormsProviderAPI.FormsColumns.JR_VERSION, formVersion);
            values.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, submissionUri);
            values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, formMediaPath);
            new FormsDao().saveForm(values);

            sbResult.append(displayName + " ");
            if (formVersion != null) {
                sbResult.append(getContext().getString(R.string.version, formVersion));
            }
            sbResult.append(getContext().getString(R.string.id, formId) + " " +
                    getContext().getString(R.string.success, getContext().getString(R.string.blank_form_count,
                            getContext().getString(R.string.received))));
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private String getFormsPath() {
        return getOdkDestinationDir() + File.separator + FORMS_DIR_NAME;
    }

    private String getInstancesPath() {
        return getOdkDestinationDir() + File.separator + INSTANCES_DIR_NAME;
    }

    private String getOdkDestinationDir() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());

        return prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR,
                getContext().getString(R.string.default_odk_destination_dir));
    }

    private void readInstances(String formId, String formVersion) {
        try {
            int numInstances = dis.readInt();
            while (numInstances-- > 0) {
                // publish current progress
                rxEventBus.post(new DownloadEvent(DownloadEvent.Status.DOWNLOADING, ++progress, total));
                String uuid = dis.readUTF();
                int mode = dis.readInt();

                String displayName = dis.readUTF();
                String submissionUri = dis.readUTF();

                if (submissionUri.equals("-1")) {
                    submissionUri = null;
                }

                Timber.d("Received uuid %s mode %s displayname %s submissionUri %s", uuid, mode, displayName, submissionUri);
                long id = new InstanceMapDao().getInstanceId(uuid);

                if (mode == ApplicationConstants.SEND_REVIEW_MODE) {
                    try (Cursor cursor = new TransferDao().getSentInstanceInstanceCursorUsingId(id)) {
                        if (id != -1 && cursor != null && cursor.getCount() > 0) {
                            // sent for review start receiving
                            Timber.d("Form sent for review");
                            dos.writeBoolean(true);
                        } else {
                            // send acknowledgement that form is not needed here
                            Timber.d("Form not sent from this device for review");
                            dos.writeBoolean(false);
                            sbResult.append(displayName + " " + getContext().getString(R.string.failed,
                                    getContext().getString(R.string.not_sent_for_review)));
                            continue;
                        }
                    }
                }

                int feedbackStatus = 0;
                String feedback = null;

                if (mode == ApplicationConstants.SEND_REVIEW_MODE) {
                    feedbackStatus = dis.readInt();
                    feedback = dis.readUTF();
                    if (feedback.equals("-1")) {
                        feedback = null;
                    }
                    Timber.d("Feedback received %s %s", feedbackStatus, feedback);
                }

                Timber.d("Feedback %s %s", feedbackStatus, feedback);

                int numRes = dis.readInt();
                String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS",
                        Locale.ENGLISH).format(Calendar.getInstance().getTime());

                String path = getInstancesPath() + File.separator + formId + "_" + time;
                String instanceFilePath = receiveFile(path);

                while (--numRes > 0) {
                    receiveFile(path);
                }

                // Add row in instances table
                ContentValues values = new ContentValues();
                values.put(DISPLAY_NAME, displayName);
                values.put(INSTANCE_FILE_PATH, path + "/" + instanceFilePath);
                values.put(STATUS, InstanceProviderAPI.STATUS_COMPLETE);
                values.put(CAN_EDIT_WHEN_COMPLETE, "true");
                values.put(SUBMISSION_URI, submissionUri);
                values.put(JR_FORM_ID, formId);
                values.put(JR_VERSION, formVersion);
                if (id == -1) {
                    // receiving first time
                    Timber.d("Writing received first time");
                    dos.writeBoolean(false);

                    Timber.d("Sending response if it exists or not receiving first time");
                    Uri uri = new InstancesDao().saveInstance(values);

                    ContentValues mapValues = new ContentValues();
                    mapValues.put(INSTANCE_UUID, uuid);
                    mapValues.put(INSTANCE_ID, Long.parseLong(uri.getLastPathSegment()));
                    new ShareDatabaseHelper(getContext()).insertMapping(mapValues);

                    // Add row in share table
                    ContentValues shareValues = new ContentValues();
                    shareValues.put(INSTANCE_ID, Long.parseLong(uri.getLastPathSegment()));
                    shareValues.put(TRANSFER_STATUS, STATUS_FORM_RECEIVE);
                    sbResult.append(displayName + getContext().getString(R.string.success,
                            getContext().getString(R.string.received_for_review)));
                    new ShareDatabaseHelper(getContext()).insertInstance(shareValues);
                } else {
                    String selection = InstanceProviderAPI.InstanceColumns._ID + "=?";
                    String[] selectionArgs = {String.valueOf(id)};
                    new InstancesDao().updateInstance(values, selection, selectionArgs);
                    TransferInstance transferInstance = new TransferDao().getSentTransferInstanceFromInstanceId(id);
                    if (mode == ApplicationConstants.SEND_REVIEW_MODE) {
                        ContentValues shareValues = new ContentValues();
                        shareValues.put(INSTRUCTIONS, feedback);
                        shareValues.put(RECEIVED_REVIEW_STATUS, feedbackStatus);
                        selection = TransferInstance.ID + " =?";
                        selectionArgs = new String[]{String.valueOf(transferInstance.getId())};
                        new TransferDao().updateInstance(shareValues, selection, selectionArgs);
                        sbResult.append(displayName + getContext().getString(R.string.success, getContext().getString(R.string.review_received)));
                    } else {

                        Timber.d("Writing received not first time");
                        dos.writeBoolean(true);
                        sbResult.append(displayName + getContext().getString(R.string.success, getContext().getString(R.string.updated)));
                    }
                }
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private String receiveFile(String path) {
        String filename = null;
        try {
            filename = dis.readUTF();
            long fileSize = dis.readLong();
            Timber.d("Size of file " + filename + " " + fileSize);
            File shareDir = new File(path);

            if (!shareDir.exists()) {
                Timber.d("Directory created " + shareDir.getPath() + " " + shareDir.mkdirs());
            }

            File newFile = new File(shareDir, filename);
            newFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(newFile);
            int n;
            byte[] buf = new byte[4096];
            while (fileSize > 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                fos.write(buf, 0, n);
                fileSize -= n;
            }
            fos.close();
            Timber.d("File created and saved " + newFile.getAbsolutePath() + " " + newFile.getName());
        } catch (IOException e) {
            Timber.e(e);
        }
        return filename;
    }
}
