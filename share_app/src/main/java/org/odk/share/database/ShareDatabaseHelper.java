package org.odk.share.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.share.application.Share;
import org.odk.share.dto.TransferInstance;

import timber.log.Timber;

import static org.odk.share.dto.InstanceMap.INSTANCE_UUID;
import static org.odk.share.dto.TransferInstance.ID;
import static org.odk.share.dto.TransferInstance.INSTANCE_ID;
import static org.odk.share.dto.TransferInstance.INSTRUCTIONS;
import static org.odk.share.dto.TransferInstance.LAST_STATUS_CHANGE_DATE;
import static org.odk.share.dto.TransferInstance.RECEIVED_REVIEW_STATUS;
import static org.odk.share.dto.TransferInstance.REVIEW_STATUS;
import static org.odk.share.dto.TransferInstance.STATUS_UNREVIEWED;
import static org.odk.share.dto.TransferInstance.TRANSFER_STATUS;
import static org.odk.share.dto.TransferInstance.VISITED_COUNT;

/**
 * Created by laksh on 6/13/2018.
 */

public class ShareDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "share.db";
    public static final String SHARE_TABLE_NAME = "transfers";
    public static final String SHARE_INSTANCE_TABLE = "map_instance";

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
                + REVIEW_STATUS + " integer, "
                + INSTRUCTIONS + " text, "
                + INSTANCE_ID + " integer not null, "
                + TransferInstance.INSTANCE_UUID + " text not null, "
                + TRANSFER_STATUS + " text not null, "
                + RECEIVED_REVIEW_STATUS + " integer,"
                + VISITED_COUNT + " integer, "
                + LAST_STATUS_CHANGE_DATE + " date not null ); ");

        db.execSQL("CREATE TABLE " + SHARE_INSTANCE_TABLE + " ("
                + ID + " integer primary key, "
                + INSTANCE_UUID + " text not null, "
                + INSTANCE_ID + " integer not null ); ");

    }

    public long insertInstance(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        if (!values.containsKey(REVIEW_STATUS)) {
            values.put(REVIEW_STATUS, STATUS_UNREVIEWED);
        }

        Long now = System.currentTimeMillis();

        if (!values.containsKey(LAST_STATUS_CHANGE_DATE)) {
            values.put(LAST_STATUS_CHANGE_DATE, now);
        }
        long id = sqLiteDatabase.insert(SHARE_TABLE_NAME, null, values);
        sqLiteDatabase.close();
        return id;
    }

    public long insertMapping(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        long id = sqLiteDatabase.insert(SHARE_INSTANCE_TABLE, null, values);
        sqLiteDatabase.close();

        return id;
    }
}