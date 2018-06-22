package org.odk.share.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by laksh on 6/13/2018.
 */

public class DatabaseContext extends ContextWrapper {
    private String path;

    public DatabaseContext(Context context, String path) {
        super(context);
        this.path = path;
    }

    @Override
    public File getDatabasePath(String name) {
        return new File(path + File.separator + name);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
       return openOrCreateDatabase(name, mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
    }
}
