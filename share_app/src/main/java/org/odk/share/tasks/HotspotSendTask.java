package org.odk.share.tasks;

import android.database.Cursor;
import android.os.AsyncTask;

import org.odk.share.dao.FormsDao;
import org.odk.share.dao.InstancesDao;
import org.odk.share.listeners.ProgressListener;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.provider.InstanceProviderAPI;

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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by laksh on 5/30/2018.
 */

public class HotspotSendTask extends AsyncTask<Long, Integer, String> {

    private ProgressListener stateListener;
    private Socket socket;
    private ServerSocket serverSocket;
    private DataOutputStream dos;
    private DataInputStream dis;

    public HotspotSendTask(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void setUploaderListener(ProgressListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.progressUpdate(values[0], values[1]);
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        stateListener.uploadingComplete(s);
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

    @Override
    protected void onCancelled() {
        super.onCancelled();
        stateListener.onCancel();
    }

    @Override
    protected String doInBackground(Long... longs) {

        try {
            Timber.d("Waiting for receiver");
            socket = serverSocket.accept();
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // show dialog and connected
            Timber.d("Start Sending");

            if (processSelectedFiles(longs)) {
                return "Successfully sent " + longs.length + " forms";
            }
        } catch (IOException e) {
            Timber.e(e);
        }

        return "Sending Failed !";
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
        Cursor c = null;
        try {
            c = new InstancesDao().getInstancesCursor(selection, selectionArgs);

            if (c != null && c.getCount() > 0) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    String formId = c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    String formVersion = c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION));

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

                    instancesList.add(c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                }
            }
            Timber.d(String.valueOf(formMap));
        } finally {
            if (c != null) {
                c.close();
            }
        }

        // send number of distinct forms
        try {
            dos.writeInt(ids.length);
            dos.writeInt(count);
        } catch (IOException e) {
            Timber.e(e);
        }

        int progress = 0;
        int total = ids.length;
        // using iterators
        Iterator<Map.Entry<String, Map<String, List<String>>>> itrId = formMap.entrySet().iterator();

        while (itrId.hasNext()) {
            Map.Entry<String, Map<String, List<String>>> mapId = itrId.next();
            Map<String, List<String>> formVersionMap =  mapId.getValue();

            Iterator<Map.Entry<String, List<String>>> itrVersion = formVersionMap.entrySet().iterator();
            while (itrVersion.hasNext()) {
                Map.Entry<String, List<String>> mapVersion = itrVersion.next();
                List<String> instanceIds = mapVersion.getValue();
                String formVers = mapVersion.getKey();
                String formId = mapId.getKey();

                Timber.d("Send form : " + formId + " " + formVers + " " + instanceIds);
                sendFormWithInstance(formId, formVers, instanceIds, progress, total);
                progress += instanceIds.size();
            }
        }
        return true;
    }

    private void sendFormWithInstance(String formId, String formVersion, List<String> instanceIds, int progress, int total) {
        try {
            Timber.d("SendFormWithInstance");
            dos.writeUTF(formId);

            if (formVersion == null) {
                dos.writeUTF("-1");
            } else {
                dos.writeUTF(formVersion);
            }

            dos.flush();

            Timber.d("Sent " + formId + " " + formVersion);

            Timber.d("Waiting for response from the receiver for " + formId + " " + formVersion);
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
        String []selectionArgs;
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

        if (cursor != null) {
            cursor.moveToPosition(-1);
            try {
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
            } finally {
                cursor.close();
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
                    String instance = c.getString(
                            c.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                    publishProgress(++progress, total);
                    Timber.d("Progress " + progress + " " + total);
                    sendInstance(instance);
                }
            }
        } catch (IOException e) {
            if (c != null) {
                c.close();
            }
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
            Timber.d("Sent message " + sentMsg);
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

                String extension = getFileExtension(fileName);

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

        if (!uploadFiles(files)) {
            return false;
        }
        return true;
    }

    boolean uploadFiles(List<File> files) {
        byte[] bytes = new byte[4096];
        try {
            int read = 0;
            dos.writeInt(files.size());

            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                DataInputStream fileInputStream = new DataInputStream(bis);
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
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

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
