package org.odk.share.tasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.odk.share.application.Share;
import org.odk.share.dao.FormsDao;
import org.odk.share.dao.InstancesDao;
import org.odk.share.events.UploadEvent;
import org.odk.share.database.ShareDatabaseHelper;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.provider.InstanceProviderAPI;
import org.odk.share.rx.RxEventBus;
import org.odk.share.utilities.ArrayUtils;
import org.odk.share.utilities.FileUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.share.dto.TransferInstance.INSTANCE_ID;
import static org.odk.share.dto.TransferInstance.STATUS_FORM_SENT;
import static org.odk.share.dto.TransferInstance.TRANSFER_STATUS;

public class UploadJob extends Job {

    public static final String TAG = "formUploadJob";
    public static final String INSTANCES = "instances";
    public static final String PORT = "port";

    @Inject
    RxEventBus rxEventBus;

    private int port;
    private Long[] instancesToSend;
    private Socket socket;
    private ServerSocket serverSocket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private int progress;
    private int total;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        ((Share) getContext().getApplicationContext()).getAppComponent().inject(this);

        initJob(params);

        rxEventBus.post(uploadInstances());

        return null;
    }

    private void initJob(Params params) {
        instancesToSend = ArrayUtils.toObject(params.getExtras().getLongArray(INSTANCES));
        port = params.getExtras().getInt(PORT, -1);
    }

    private UploadEvent uploadInstances() {
        try {
            Timber.d("Waiting for receiver");

            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // show dialog and connected
            Timber.d("Start Sending");
            processSelectedFiles(instancesToSend);

            // close connection
            socket.close();
            serverSocket.close();
            dos.close();
            dis.close();
        } catch (IOException e) {
            Timber.e(e);
            return new UploadEvent(UploadEvent.Status.ERROR, e.getMessage());
        }

        return new UploadEvent(UploadEvent.Status.FINISHED, String.valueOf(progress));
    }

    @Override
    protected void onCancel() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
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

    private boolean processSelectedFiles(Long[] ids) {

        // map that stores key as formId and value is another map which contains version as key and List with instances as value
        Map<String, Map<String, List<String>>> formMap = new HashMap<>();
        StringBuilder selectionBuf = new StringBuilder(InstanceProviderAPI.InstanceColumns._ID + " IN (");
        String[] selectionArgs = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                selectionBuf.append(",");
            }
            selectionBuf.append("?");
            selectionArgs[i] = ids[i].toString();
        }

        selectionBuf.append(")");
        String selection = selectionBuf.toString();

        int count = 0;
        try (Cursor cursor = new InstancesDao().getInstancesCursor(selection, selectionArgs)) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    String formId = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    String formVersion = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION));

                    Map<String, List<String>> instanceMap;
                    if (formMap.containsKey(formId)) {
                        instanceMap = formMap.get(formId);
                    } else {
                        instanceMap = new HashMap<>();
                        formMap.put(formId, instanceMap);
                    }

                    List<String> instancesList;
                    if (instanceMap.containsKey(formVersion)) {
                        instancesList = instanceMap.get(formVersion);
                    } else {
                        instancesList = new ArrayList<>();
                        instanceMap.put(formVersion, instancesList);
                        count++;
                    }

                    instancesList.add(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                }
            }
            Timber.d(String.valueOf(formMap));
        }

        // send number of distinct forms
        try {
            dos.writeInt(ids.length);
            dos.writeInt(count);
        } catch (IOException e) {
            Timber.e(e);
        }

        total = ids.length;
        // using iterators

        for (Map.Entry<String, Map<String, List<String>>> mapId : formMap.entrySet()) {
            Map<String, List<String>> formVersionMap = mapId.getValue();

            for (Map.Entry<String, List<String>> mapVersion : formVersionMap.entrySet()) {
                List<String> instanceIds = mapVersion.getValue();
                String formVers = mapVersion.getKey();
                String formId = mapId.getKey();
                sendFormWithInstance(formId, formVers, instanceIds);
                progress += instanceIds.size();
            }
        }
        return true;
    }

    private void sendFormWithInstance(String formId, String formVersion, List<String> instanceIds) {
        try {
            Timber.d("SendFormWithInstance");
            dos.writeUTF(formId);

            if (formVersion == null) {
                dos.writeUTF("-1");
            } else {
                dos.writeUTF(formVersion);
            }

            dos.flush();

            Timber.d("Waiting for response from the receiver for %s %s ", formId, formVersion);
            while (dis.available() <= 0) {
                continue;
            }

            boolean formExistAtReceiver = dis.readBoolean();
            Timber.d("Form exists " + formExistAtReceiver);

            if (!formExistAtReceiver) {
                sendForm(formId, formVersion);
                Timber.d("Form Sent");
            }

            Timber.d("Sending Instances");
            sendInstances(instanceIds, progress, total);
            Timber.d("Instanes sent");
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void sendForm(String formId, String formVersion) {
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

        try (Cursor cursor = new FormsDao().getFormsCursor(null, selection, selectionArgs, null)) {
            cursor.moveToPosition(-1);

            if (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
                String formMediaPath = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH));
                String formFilePath = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH));
                String submissionUri = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.SUBMISSION_URI));

                try {
                    dos.writeUTF(displayName);
                    dos.writeUTF(formId);

                    if (formVersion == null) {
                        dos.writeUTF("-1");
                    } else {
                        dos.writeUTF(formVersion);
                    }

                    if (submissionUri == null) {
                        dos.writeUTF("-1");
                    } else {
                        dos.writeUTF(submissionUri);
                    }

                    // form file sent
                    sendFile(formFilePath);

                    // send form resources
                    File[] formRes = getFormResources(formMediaPath);

                    if (formRes != null) {
                        dos.writeInt(formRes.length);
                        for (File f : formRes) {
                            String fileName = f.getName();
                            sendFile(formMediaPath + "/" + fileName);
                        }
                    } else {
                        dos.writeInt(0);
                    }
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
        }
    }

    private void sendInstances(List<String> instanceIds, int progress, int total) {
        StringBuilder selectionBuf = new StringBuilder(InstanceProviderAPI.InstanceColumns._ID + " IN (");
        String[] selectionArgs = new String[instanceIds.size()];
        for (int i = 0; i < instanceIds.size(); i++) {
            if (i > 0) {
                selectionBuf.append(",");
            }
            selectionBuf.append("?");
            selectionArgs[i] = instanceIds.get(i);
        }

        selectionBuf.append(")");
        String selection = selectionBuf.toString();
        Cursor c = null;
        try {
            c = new InstancesDao().getInstancesCursor(selection, selectionArgs);

            if (c != null && c.getCount() > 0) {
                dos.writeInt(c.getCount());
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    String displayName = c.getString(
                            c.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    String submissionUri = c.getString(
                            c.getColumnIndex(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI));

                    dos.writeUTF(displayName);

                    if (submissionUri == null) {
                        dos.writeUTF("-1");
                    } else {
                        dos.writeUTF(submissionUri);
                    }

                    rxEventBus.post(new UploadEvent(UploadEvent.Status.UPLOADING, ++progress, total));
                    String instance = c.getString(
                            c.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

                    sendInstance(instance);

                    // add row in share table
                    ContentValues values = new ContentValues();
                    values.put(INSTANCE_ID,
                            c.getLong(c.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                    values.put(TRANSFER_STATUS, STATUS_FORM_SENT);
                    new ShareDatabaseHelper(getContext()).insertInstance(values);
                }
            }
        } catch (IOException e) {
            c.close();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void sendFile(String filePath) {
        byte[] bytes = new byte[4096];
        try {
            File file = new File(filePath);
            int read = 0;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream fileInputStream = new DataInputStream(bis);
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());
            while ((read = fileInputStream.read(bytes)) > 0) {
                dos.write(bytes, 0, read);
            }
            final String sentMsg = "File sent to: " + socket;
            Timber.d("Sent message %s ", sentMsg);
        } catch (FileNotFoundException e1) {
            Timber.e(e1);
        } catch (IOException e1) {
            Timber.e(e1);
        }
    }

    private File[] getFormResources(String formResPath) {
        File directory = new File(formResPath);
        return directory.listFiles();
    }

    private boolean sendInstance(String instanceFilePath) {
        File instanceFile = new File(instanceFilePath);
        File[] allFiles = instanceFile.getParentFile().listFiles();

        // add media files
        List<File> files = new ArrayList<File>();
        files.add(instanceFile);
        if (allFiles != null) {
            for (File f : allFiles) {
                String fileName = f.getName();

                if (fileName.startsWith(".")) {
                    continue; // ignore invisible files
                } else if (fileName.equals(instanceFile.getName())) {
                    continue; // the xml file has already been added
                } else if (fileName.equals(instanceFile.getName())) {
                    continue; // the xml file has already been added
                }

                String extension = FileUtils.getFileExtension(fileName);

                if (extension.equals("jpg")) { // legacy 0.9x
                    files.add(f);
                } else if (extension.equals("3gpp")) { // legacy 0.9x
                    files.add(f);
                } else if (extension.equals("3gp")) { // legacy 0.9x
                    files.add(f);
                } else if (extension.equals("mp4")) { // legacy 0.9x
                    files.add(f);
                } else if (extension.equals("osm")) { // legacy 0.9x
                    files.add(f);
                } else {
                    Timber.d("unrecognized file type " + f.getName());
                }
            }
        }
        Timber.d("Files : " + files);
        return uploadFiles(files);
    }

    boolean uploadFiles(List<File> files) {
        byte[] bytes = new byte[4096];
        try {
            int read = 0;
            dos.writeInt(files.size());
            Timber.d("File size : " + files.size());
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
                DataInputStream fileInputStream = new DataInputStream(bis);
                Timber.d("Name " + file.getName() + " " + file.length());
                while ((read = fileInputStream.read(bytes)) > 0) {
                    dos.write(bytes, 0, read);
                }
                final String sentMsg = "File sent to: " + socket;
                Timber.d("Sent message " + sentMsg);
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
