package org.odk.share.views.ui.review;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.views.ui.common.injectable.InjectableActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.dto.TransferInstance.LAST_STATUS_CHANGE_DATE;

public class ReviewFormActivity extends InjectableActivity {

    @BindView(R.id.bApprove)
    Button approveButton;
    @BindView(R.id.bReject)
    Button rejectButton;
    @BindView(R.id.bReviewLater)
    Button reviewLaterButton;
    @BindView(R.id.bViewAgain)
    Button viewForm;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.save_feedback)
    EditText feedback;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    InstancesDao instancesDao;

    @Inject
    TransferDao transferDao;

    public static final String TRANSFER_ID = "transfer_id";
    public static final String INSTANCE_ID = "instance_id";

    private long transferID;
    private long instanceID;
    private int visitedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_form);
        ButterKnife.bind(this);

        setTitle(getString(R.string.review_form));
        setSupportActionBar(toolbar);

        transferID = getIntent().getLongExtra(TRANSFER_ID, -1);
        instanceID = getIntent().getLongExtra(INSTANCE_ID, -1);

        if (transferID == -1 || instanceID == -1) {
           finish();
        }

        Cursor cursor = transferDao.getInstanceCursorFromId(transferID);
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                visitedCount = cursor.getInt(cursor.getColumnIndex(TransferInstance.VISITED_COUNT));

                int formStatus = cursor.getInt(cursor.getColumnIndex(TransferInstance.REVIEW_STATUS));

                if (formStatus == TransferInstance.STATUS_UNREVIEWED) {
                    viewFormInCollect();
                } else {
                    String feedbackText = cursor.getString(cursor.getColumnIndex(TransferInstance.INSTRUCTIONS));
                    if (feedbackText != null) {
                        feedback.setText(feedbackText);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    @Override
    protected void onResume() {
        Cursor cursor = instancesDao.getInstancesCursorForId(String.valueOf(instanceID));
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                description.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));
            } finally {
                cursor.close();
            }
        }

        super.onResume();
    }

    private void launchCollect() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://org.odk.collect.android.provider.odk.instances/instances/" + instanceID));
        intent.putExtra("formMode", "viewSent");
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, getString(R.string.collect_not_installed), Toast.LENGTH_LONG).show();
        } else {
            startActivity(intent);
        }
    }

    @OnClick(R.id.bApprove)
    public void acceptForm() {
        String feedbackText = feedback.getText().toString();
        if (feedback != null) {
            ContentValues values = new ContentValues();
            values.put(TransferInstance.INSTRUCTIONS, feedbackText);
            values.put(TransferInstance.REVIEW_STATUS, TransferInstance.STATUS_ACCEPTED);

            Long now = System.currentTimeMillis();
            values.put(LAST_STATUS_CHANGE_DATE, now);

            String where = TransferInstance.ID + "=?";
            String[] whereArgs = {
                    String.valueOf(transferID)
            };
            transferDao.updateInstance(values, where, whereArgs);
        }
        Toast.makeText(this, getString(R.string.form_approved), Toast.LENGTH_LONG).show();
        finish();
    }

    @OnClick(R.id.bReject)
    public void rejectForm() {
        String feedbackText = feedback.getText().toString();
        if (feedback != null) {
            ContentValues values = new ContentValues();
            values.put(TransferInstance.INSTRUCTIONS, feedbackText);
            values.put(TransferInstance.REVIEW_STATUS, TransferInstance.STATUS_REJECTED);
            Long now = System.currentTimeMillis();
            values.put(LAST_STATUS_CHANGE_DATE, now);

            String where = TransferInstance.ID + "=?";
            String[] whereArgs = {
                    String.valueOf(transferID)
            };
            transferDao.updateInstance(values, where, whereArgs);
        }
        Toast.makeText(this, getString(R.string.form_rejected), Toast.LENGTH_LONG).show();
        finish();
    }

    @OnClick(R.id.bReviewLater)
    public void reviewFormLater() {
        finish();
    }

    @OnClick(R.id.bViewAgain)
    public void viewFormInCollect() {
        Cursor cursor = transferDao.getInstanceCursorFromId(transferID);
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                visitedCount = cursor.getInt(cursor.getColumnIndex(TransferInstance.VISITED_COUNT));
            } finally {
                cursor.close();
            }
        }
        ContentValues values = new ContentValues();
        values.put(TransferInstance.VISITED_COUNT, visitedCount + 1);
        String where = TransferInstance.ID + "=?";
        String[] whereArgs = {
                String.valueOf(transferID)
        };
        transferDao.updateInstance(values, where, whereArgs);
        launchCollect();
    }
}
