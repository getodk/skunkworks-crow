package org.odk.share.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.dao.InstancesDao;
import org.odk.share.provider.InstanceProviderAPI;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReviewFormActivity extends AppCompatActivity {

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

    public static final String TRANSFER_ID = "transfer_id";
    public static final String INSTANCE_ID = "instance_id";

    private long transferID;
    private long instanceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_form);
        ButterKnife.bind(this);

        transferID = getIntent().getLongExtra(TRANSFER_ID, -1);
        instanceID = getIntent().getLongExtra(INSTANCE_ID, -1);

        if (transferID == -1 || instanceID == -1) {
           finish();
        }
        launchCollect();
    }

    @Override
    protected void onResume() {
        Cursor cursor = new InstancesDao().getInstancesCursorForId(String.valueOf(instanceID));
        if (cursor != null) {
            cursor.moveToFirst();
            description.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));
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
    }

    @OnClick(R.id.bReject)
    public void rejectForm() {
    }

    @OnClick(R.id.bReviewLater)
    public void reviewFormLater() {
        finish();
    }

    @OnClick(R.id.bViewAgain)
    public void viewFormAgain() {
        launchCollect();
    }
}
