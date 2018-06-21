package org.odk.share.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.share.application.Share;

import timber.log.Timber;

import static org.odk.share.dto.TransferInstance.ID;
import static org.odk.share.dto.TransferInstance.INSTANCE_ID;
import static org.odk.share.dto.TransferInstance.INSTRUCTIONS;
import static org.odk.share.dto.TransferInstance.LAST_STATUS_CHANGE_DATE;
import static org.odk.share.dto.TransferInstance.REVIEWED;
import static org.odk.share.dto.TransferInstance.TRANSFER_STATUS;

/**
 * Created by laksh on 6/13/2018.
 */

public class ShareDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "share.db";
    private static final String SHARE_TABLE_NAME = "transfers";

    private static final int DATABASE_VERSION = 1;

    public ShareDatabaseHelper(Context context) {
        super(new DatabaseContext(context, Share.METADATA_PATH), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createInstancesTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Timber.d("onUpgrade -- OldVersion: %s, NewVersion: %s", oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + SHARE_TABLE_NAME);
        onCreate(db);
    }

    private void createInstancesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SHARE_TABLE_NAME + " ("
                + ID + " integer primary key, "
                + REVIEWED + " boolean, "
                + INSTRUCTIONS + " text, "
                + INSTANCE_ID + " integer not null, "
                + TRANSFER_STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null ); ");

    }

    public long insertInstance(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        if (!values.containsKey(REVIEWED)) {
            values.put(REVIEWED, false);
        }

        Long now = System.currentTimeMillis();

        if (!values.containsKey(LAST_STATUS_CHANGE_DATE)) {
            values.put(LAST_STATUS_CHANGE_DATE, now);
        }
        long id = sqLiteDatabase.insert(SHARE_TABLE_NAME, null, values);
        sqLiteDatabase.close();
        return id;
    }
}