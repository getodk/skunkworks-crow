package org.odk.share.tasks;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import org.odk.share.dao.FormsDao;
import org.odk.share.listeners.ProgressListener;
import org.odk.share.provider.FormsProviderAPI;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by laksh on 5/31/2018.
 */

public class WifiReceiveTask extends AsyncTask<String, Integer, String> {

    private String ip;
    private int port;
    private ProgressListener stateListener;
    private DataInputStream dis;
    private DataOutputStream dos;
    private int total;
    private int progress;

    private static final String INSTANCE_PATH = "share/instances/";
    private static final String FORM_PATH = "share/forms/";

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

    public WifiReceiveTask(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private String receiveForms() {
        Socket socket = null;
        Timber.d("Socket " + ip + " " + port);

        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(socket.getOutputStream());
            total = dis.readInt();
            int num = dis.readInt();
            Timber.d("Number of forms" + num + " ");
            while (num-- > 0) {
                Timber.d("Reading form");
                if (readFormAndInstances()) {
                    return String.valueOf(progress);
                }
            }
        } catch (UnknownHostException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        }

        return  String.valueOf(progress);
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
            Timber.d("Form exits " + formExists);
            dos.writeBoolean(formExists);

            if (!formExists) {
                // read form
                readForm();
            }

            // readInstances
            readInstances(formId);
            return true;
        } catch (IOException e) {
            Timber.e(e);
        }
        return false;
    }

    private boolean isFormExits(String formId, String formVersion) {
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

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
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
            receiveFile(FORM_PATH);
            int numOfRes = dis.readInt();
            while (numOfRes-- > 0) {
                receiveFile(FORM_PATH + displayName + "-media");
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void readInstances(String formId) {
        try {
            int numInstances = dis.readInt();
            while (numInstances-- > 0) {
                publishProgress(++progress, total);
                int numRes = dis.readInt();
                String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS",
                        Locale.ENGLISH).format(Calendar.getInstance().getTime());
                String path = INSTANCE_PATH + formId + "_" + time;
                while (numRes-- > 0) {
                    receiveFile(path);
                }
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void receiveFile(String path) {
        try {
            String filename = dis.readUTF();
            long fileSize = dis.readLong();
            Timber.d("Size of file " + filename + " " + fileSize);
            File shareDir = new File(Environment.getExternalStorageDirectory(), path);

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
    }

    @Override
    protected String doInBackground(String... strings) {
        return receiveForms();
    }

    @Override
    protected void onPostExecute(String s) {
        stateListener.uploadingComplete(s);
    }

}
