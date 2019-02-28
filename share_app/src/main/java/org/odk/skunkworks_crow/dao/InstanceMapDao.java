package org.odk.skunkworks_crow.dao;

import android.content.ContentValues;
import android.database.Cursor;

import org.odk.skunkworks_crow.application.Share;

import java.util.HashMap;

import timber.log.Timber;

import static org.odk.skunkworks_crow.dto.InstanceMap.INSTANCE_ID;
import static org.odk.skunkworks_crow.dto.InstanceMap.INSTANCE_UUID;
import static org.odk.skunkworks_crow.provider.InstanceMapProvider.CONTENT_URI;


/**
 * Created by laksh on 8/2/2018.
 */

public class InstanceMapDao {

    public int updateInstance(ContentValues values, String where, String[] whereArgs) {
        return Share.getInstance().getContentResolver().update(CONTENT_URI, values, where, whereArgs);
    }

    public HashMap<Long, String> getInstanceMap() {
        Cursor cursor = getInstancesCursor(null, null, null, null);

        HashMap<Long, String> instanceMap = new HashMap<>();
        if (cursor != null) {
            Timber.d("CUrsor " + cursor.getCount());
            try {
                while (cursor.moveToNext()) {
                    long instanceId = cursor.getLong(cursor.getColumnIndex(INSTANCE_ID));
                    String uuid = cursor.getString(cursor.getColumnIndex(INSTANCE_UUID));
                    instanceMap.put(instanceId, uuid);
                }
            } finally {
                cursor.close();
            }
        }
        return instanceMap;
    }

    public HashMap<String, Long> getInstanceUUIDMap() {
        Cursor cursor = getInstancesCursor(null, null, null, null);

        HashMap<String, Long> instanceMap = new HashMap<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long instanceId = cursor.getLong(cursor.getColumnIndex(INSTANCE_ID));
                    String uuid = cursor.getString(cursor.getColumnIndex(INSTANCE_UUID));
                    instanceMap.put(uuid, instanceId);
                }
            } finally {
                cursor.close();
            }
        }
        return instanceMap;
    }

    public Cursor getInstancesCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Share.getInstance().getContentResolver()
                .query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public long getInstanceId(String uuid) {
        String selection = INSTANCE_UUID + "=?";
        String[] selectionArgs = {uuid};

        Cursor cursor = getInstancesCursor(null, selection, selectionArgs, null);
        long id = -1;

        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndex(INSTANCE_ID));
                }
            } finally {
                cursor.close();
            }
        }
        return id;
    }
}