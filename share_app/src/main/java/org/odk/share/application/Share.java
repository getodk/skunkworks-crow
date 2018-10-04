package org.odk.share.application;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;
import android.os.Environment;
import org.odk.share.R;

import java.io.File;

import org.odk.share.injection.config.AppComponent;
import org.odk.share.injection.config.DaggerAppComponent;
import org.odk.share.tasks.ShareJobCreator;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import timber.log.Timber;

/**
 * Created by laksh on 5/19/2018.
 */

public class Share extends DaggerApplication {

    private static Share singleton = null;
    private AppComponent appComponent;

    public static Share getInstance() {
        return singleton;
    }

    public static final String ODK_ROOT = Environment.getExternalStorageDirectory() + File.separator + "share";
    public static final String ODK_COLLECT_ROOT = Environment.getExternalStorageDirectory() + File.separator + "odk";
    public static final String FORMS_DIR_NAME = "forms";
    public static final String INSTANCES_DIR_NAME = "instances";
    public static final String METADATA_DIR_NAME = "metadata";
    public static final String FORMS_PATH = ODK_COLLECT_ROOT + File.separator + FORMS_DIR_NAME;
    public static final String INSTANCES_PATH = ODK_COLLECT_ROOT + File.separator + INSTANCES_DIR_NAME;
    public static final String METADATA_PATH = ODK_ROOT + File.separator + METADATA_DIR_NAME;

    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;
        Timber.plant(new Timber.DebugTree());

        try {
            JobManager jobManager = JobManager.create(this);
            jobManager.cancelAll();
            jobManager.addJobCreator(new ShareJobCreator());
        } catch (JobManagerCreateException e) {
            Timber.e(e);
        }
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        appComponent = DaggerAppComponent.builder().application(this).build();
        return appComponent;
    }

    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public static void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
            if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
                    throw new RuntimeException(Share.getInstance().getString(R.string.sdcard_unmounted, cardstatus));
            }
            String[] dirs = {ODK_ROOT, FORMS_PATH, INSTANCES_PATH, METADATA_PATH};
            for (String dirName : dirs) {
                File dir = new File(dirName);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        String message = getInstance().getString(R.string.cannot_create_directory, dirName);
                        Timber.w(message);
                        throw new RuntimeException(message);
                    }
                } else {
                    if (!dir.isDirectory()) {
                        String message = getInstance().getString(R.string.not_a_directory, dirName);
                        Timber.w(message);
                        throw new RuntimeException(message);
                    }
                }
            }
    }
}
