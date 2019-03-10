package org.odk.share.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import org.odk.share.application.Share;
import org.odk.share.database.ShareDatabaseHelper;
import org.odk.share.dto.TransferInstance;

import androidx.annotation.NonNull;

import static org.odk.share.database.ShareDatabaseHelper.SHARE_INSTANCE_TABLE;

/**
 * Created by laksh on 8/2/2018.
 */

public class InstanceMapProvider extends ContentProvider {

    private static final int INSTANCE_MAP = 1;
    private static final int INSTANCE_MAP_ID = 2;

    private static final String AUTHORITY = "org.odk.share.provider.odk.map";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/instance");

    private static final UriMatcher sUriMatcher;

    private ShareDatabaseHelper databaseHelper;

    private ShareDatabaseHelper getDbHelper() {

        try {
            Share.createODKDirs(getContext());
        } catch (RuntimeException e) {
            databaseHelper = null;
            return null;
        }

        if (databaseHelper != null) {
            return databaseHelper;
        }
        databaseHelper = new ShareDatabaseHelper(getContext());
        return databaseHelper;
    }

    @Override
    public boolean onCreate() {
        ShareDatabaseHelper helper = getDbHelper();
        return helper != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SHARE_INSTANCE_TABLE);

        switch (sUriMatcher.match(uri)) {
            case INSTANCE_MAP:
                break;
            case INSTANCE_MAP_ID:
                qb.appendWhere(TransferInstance.ID + "="
                        + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = null;
        ShareDatabaseHelper shareDatabaseHelper = getDbHelper();
        if (shareDatabaseHelper != null) {
            c = qb.query(shareDatabaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public synchronized Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != INSTANCE_MAP) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ShareDatabaseHelper shareDatabaseHelper = getDbHelper();
        if (shareDatabaseHelper != null) {
            ContentValues values;
            if (initialValues != null) {
                values = new ContentValues(initialValues);
            } else {
                values = new ContentValues();
            }

            long rowId = shareDatabaseHelper.getWritableDatabase().insert(SHARE_INSTANCE_TABLE, null, values);
            if (rowId > 0) {
                Uri instanceUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(instanceUri, null);
                return instanceUri;
            }
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        int count = 0;
        ShareDatabaseHelper shareDatabaseHelper = getDbHelper();
        if (shareDatabaseHelper != null) {
            SQLiteDatabase db = shareDatabaseHelper.getWritableDatabase();

            switch (sUriMatcher.match(uri)) {
                case INSTANCE_MAP:
                    count = db.delete(SHARE_INSTANCE_TABLE, where, whereArgs);
                    break;

                case INSTANCE_MAP_ID:
                    String formId = uri.getLastPathSegment();
                    count = db.delete(
                            SHARE_INSTANCE_TABLE,
                            TransferInstance.ID
                                    + "="
                                    + formId
                                    + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        int count = 0;
        ShareDatabaseHelper shareDatabaseHelper = getDbHelper();
        if (shareDatabaseHelper != null) {
            SQLiteDatabase db = shareDatabaseHelper.getWritableDatabase();
            switch (sUriMatcher.match(uri)) {
                case INSTANCE_MAP:
                    count = db.update(SHARE_INSTANCE_TABLE, values, where, whereArgs);
                    break;

                case INSTANCE_MAP_ID:
                    String formId = uri.getLastPathSegment();
                    count = db.update(
                            SHARE_INSTANCE_TABLE,
                            values,
                            TransferInstance.ID
                                    + "="
                                    + formId
                                    + (!TextUtils.isEmpty(where) ? " AND ("
                                    + where + ')' : ""), whereArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "instance", INSTANCE_MAP);
        sUriMatcher.addURI(AUTHORITY, "instance/#", INSTANCE_MAP_ID);
    }

}
