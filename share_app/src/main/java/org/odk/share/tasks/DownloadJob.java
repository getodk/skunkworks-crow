package org.odk.share.tasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.odk.share.application.Share;
import org.odk.share.dao.FormsDao;
import org.odk.share.events.DownloadEvent;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.rx.RxEventBus;
import org.odk.share.dao.InstancesDao;
import org.odk.share.database.ShareDatabaseHelper;
import org.odk.share.provider.InstanceProviderAPI;

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

import static org.odk.share.application.Share.FORMS_PATH;
import static org.odk.share.application.Share.INSTANCES_PATH;
import static org.odk.share.dto.TransferInstance.INSTANCE_ID;
import static org.odk.share.dto.TransferInstance.STATUS_FORM_RECEIVE;
import static org.odk.share.dto.TransferInstance.TRANSFER_STATUS;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.SUBMISSION_URI;

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

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        ((Share) getContext().getApplicationContext()).getAppComponent().inject(this);

        initJob(params);

        String result = receiveForms();
        rxEventBus.post(new DownloadEvent(DownloadEvent.Status.FINISHED, result));

        return null;
    }

    private void initJob(Params params) {
        ip = params.getExtras().getString(IP, "");
        port = params.getExtras().getInt(PORT, -1);
    }

    private String receiveForms() {
        Timber.d("Socket " + ip + " " + port);

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
            Timber.d("Socket connected");
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(socket.getOutputStream());
            total = dis.readInt();
            int num = dis.readInt();
            Timber.d("Number of forms : %d", num);
            for (int i = 0; i < num; i++) {
                Timber.d("Downloading form : %d", i + 1);
                boolean result = readFormAndInstances();
                Timber.d("Form %d downloaded = %s", i + 1, result);
            }

            // close connection
            socket.close();
            dos.close();
            dis.close();

        } catch (IOException e) {
            Timber.e(e);
        }

        return String.valueOf(progress);
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
            String formName = receiveFile(FORMS_PATH);
            int numOfRes = dis.readInt();
            String formMediaPath = FORMS_PATH + "/" + displayName + "-media";
            while (numOfRes-- > 0) {
                receiveFile(formMediaPath);
            }

            // Add row in forms db
            ContentValues values = new ContentValues();
            values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FORMS_PATH + "/" + formName);
            values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, displayName);
            values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, formId);
            values.put(FormsProviderAPI.FormsColumns.JR_VERSION, formVersion);
            values.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, submissionUri);
            values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, formMediaPath);
            Timber.d("form %s" ,new FormsDao().saveForm(values));
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void readInstances(String formId, String formVersion) {
        try {
            int numInstances = dis.readInt();
            while (numInstances-- > 0) {
                // publish current progress
                rxEventBus.post(new DownloadEvent(DownloadEvent.Status.DOWNLOADING, ++progress, total));
                String displayName = dis.readUTF();
                String submissionUri = dis.readUTF();

                if (submissionUri.equals("-1")) {
                    submissionUri = null;
                }

                int numRes = dis.readInt();
                String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS",
                        Locale.ENGLISH).format(Calendar.getInstance().getTime());
                String path = INSTANCES_PATH + "/" + formId + "_" + time;
                while (numRes-- > 0) {
                    receiveFile(path);
                }

                // Add row in instances table
                ContentValues values = new ContentValues();
                values.put(DISPLAY_NAME, displayName);
                values.put(STATUS, InstanceProviderAPI.STATUS_COMPLETE);
                values.put(CAN_EDIT_WHEN_COMPLETE, "true");
                values.put(SUBMISSION_URI, submissionUri);
                values.put(INSTANCE_FILE_PATH, path);
                values.put(JR_FORM_ID, formId);
                values.put(JR_VERSION, formVersion);
                Uri uri = new InstancesDao().saveInstance(values);

                // Add row in share table
                ContentValues shareValues = new ContentValues();
                shareValues.put(INSTANCE_ID, Long.parseLong(uri.getLastPathSegment()));
                shareValues.put(TRANSFER_STATUS, STATUS_FORM_RECEIVE);
                new ShareDatabaseHelper().insertInstance(shareValues);
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
