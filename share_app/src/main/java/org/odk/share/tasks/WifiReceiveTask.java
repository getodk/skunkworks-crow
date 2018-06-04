package org.odk.share.tasks;

import android.os.AsyncTask;
import android.os.Environment;
import org.odk.share.listeners.ProgressListener;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by laksh on 5/31/2018.
 */

public class WifiReceiveTask extends AsyncTask<String, Integer, String> {

    String ip;
    int port;
    ProgressListener stateListener;

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

    private String readData() {
        Socket socket = null;
        int fileNo;
        List<File> fileList = new ArrayList<>();
        String dialogMessage = null;
        ArrayList<String> filesDownloaded = new ArrayList<>();
        Timber.d("Socket" + ip + " " + port);
        try {
            socket = new Socket(ip, port);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            int num = dis.readInt();
            Timber.d("Number of forms" + num + " ");
            while (num-- > 0) {
                String name1 = dis.readUTF();
                String name2 = dis.readUTF();
                String name3 = dis.readUTF();

                Timber.d(name1 + " " + name2 + " " + name3);

                fileNo = dis.readInt();
                Timber.d("FILE " + fileNo);
                fileList.clear();
                boolean checkFormName = false;
                while (fileNo-- > 0) {
                    String filename = dis.readUTF();
                    Timber.d("File" + filename);
                    if (!checkFormName) {
                        checkFormName = true;
                        filesDownloaded.add(filename);
                        Timber.d("LIST" + filesDownloaded + " " + filesDownloaded.size());
                    }
                    long fileSize = dis.readLong();
                    File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
                    newFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int n;
                    byte[] buf = new byte[4096];
                    while (fileSize > 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                        fos.write(buf, 0, n);
                        fileSize -= n;
                    }
                    fos.close();
                    fileList.add(newFile);
                    Timber.d("File created", filename);
                }
            }


        } catch (IOException e) {

            // Connection Interrupted
            // Make sure to remove each resource for forms which are received incomplete
            for (File file : fileList) {
                if (dialogMessage == null) {
                    dialogMessage = "Files Deleted : " + file.getName() + "\n";
                } else {
                    dialogMessage += file.getName();
                }
                Timber.d("==Delete " + file.getName() + " " + file.delete());
            }
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                Timber.e(e);
            }
        }
        boolean messageDownload = false;
        for (String name : filesDownloaded) {
            if (dialogMessage == null) {
                messageDownload = true;
                dialogMessage = "Files Downloaded : ";
            } else if (!messageDownload) {
                messageDownload = true;
                dialogMessage = "\nFiles Downloaded : ";
            }
            dialogMessage += name + " ";
            Timber.d("Message " + dialogMessage);

        }
        return dialogMessage;
    }

    @Override
    protected String doInBackground(String... strings) {
        return readData();
    }

    @Override
    protected void onPostExecute(String s) {
        stateListener.uploadingComplete(s);
    }

}
