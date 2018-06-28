package org.odk.share.dao;

import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import org.odk.share.application.Share;
import org.odk.share.dto.TransferInstance;

import static org.odk.share.provider.TransferProvider.CONTENT_URI;

/**
 * Created by laksh on 6/28/2018.
 */

public class TransferDao {

    public Cursor getSentInstancesCursor() {
        String selection = TransferInstance.TRANSFER_STATUS + " =? ";
        String[] selectionArgs = {TransferInstance.STATUS_FORM_SENT};
        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getReceiveInstancesCursor() {
        String selection = TransferInstance.TRANSFER_STATUS + " =? ";
        String[] selectionArgs = {TransferInstance.STATUS_FORM_RECEIVE};
        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getReviewedInstancesCursor() {
        String selection = TransferInstance.REVIEWED + " =? ";
        String[] selectionArgs = {"1"};
        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getUnreviewedInstancesCursor() {
        String selection = TransferInstance.REVIEWED + " =? ";
        String[] selectionArgs = {"0"};
        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public CursorLoader getSentInstancesCursorLoader(String sortOrder) {
        String selection = TransferInstance.TRANSFER_STATUS + " =? ";
        String[] selectionArgs = {TransferInstance.STATUS_FORM_SENT};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getReceiveInstancesCursorLoader(String sortOrder) {
        String selection = TransferInstance.TRANSFER_STATUS + " =? ";
        String[] selectionArgs = {TransferInstance.STATUS_FORM_RECEIVE};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public Cursor getInstancesCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Share.getInstance().getContentResolver()
                .query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getInstancesCursorLoader(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(Share.getInstance(), CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }
}