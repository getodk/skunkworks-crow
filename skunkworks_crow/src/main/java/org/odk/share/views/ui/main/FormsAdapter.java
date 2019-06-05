package org.odk.share.views.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.views.ui.common.basecursor.BaseCursorViewHolder;
import org.odk.share.views.ui.common.basecursor.CursorRecyclerViewAdapter;
import org.odk.share.views.listeners.ItemClickListener;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;

import java.util.LinkedHashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FormsAdapter extends CursorRecyclerViewAdapter<FormsAdapter.FormHolder> {

    private final InstancesDao instancesDao;
    private final TransferDao transferDao;
    @Nullable
    private final LinkedHashSet<Long> selectedForms;

    public FormsAdapter(Context context, Cursor cursor, ItemClickListener listener, InstancesDao instancesDao, TransferDao transferDao) {
        this(context, cursor, listener, null, instancesDao, transferDao);
    }

    public FormsAdapter(Context context, Cursor cursor, ItemClickListener listener, @Nullable LinkedHashSet<Long> selectedForms, InstancesDao instancesDao, TransferDao transferDao) {
        super(context, cursor, listener);
        this.selectedForms = selectedForms;
        this.instancesDao = instancesDao;
        this.transferDao = transferDao;
    }

    @Override
    public void onBindViewHolder(FormHolder viewHolder, Cursor cursor) {
        int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
        int displayNameColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME);
        int jrFormIdColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
        int jrVersionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION);

        Form form = new Form.Builder()
                .id(cursor.getInt(idColumnIndex))
                .displayName(cursor.getString(displayNameColumnIndex))
                .jrFormId(cursor.getString(jrFormIdColumnIndex))
                .jrVersion(cursor.getString(jrVersionColumnIndex))
                .build();

        viewHolder.setForm(form);

        viewHolder.tvTitle.setText(form.getDisplayName());

        StringBuilder sb = new StringBuilder();
        if (form.getJrVersion() != null) {
            sb.append(context.getString(R.string.version, form.getJrVersion()));
        }
        sb.append(context.getString(R.string.id, form.getJrFormId()));

        viewHolder.tvSubtitle.setText(sb.toString());
        viewHolder.checkBox.setVisibility(selectedForms != null ? View.VISIBLE : View.GONE);
        viewHolder.checkBox.setChecked(selectedForms != null && selectedForms.contains(((long) form.getId())));
        viewHolder.filledIcon.setImageResource(R.drawable.blank_form);

        if (selectedForms == null) {
            String[] selectionArgs;
            String selection;

            if (form.getJrVersion() == null) {
                selectionArgs = new String[]{form.getJrFormId()};
                selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                        + InstanceProviderAPI.InstanceColumns.JR_VERSION + " IS NULL";
            } else {
                selectionArgs = new String[]{form.getJrFormId(), form.getJrVersion()};
                selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                        + InstanceProviderAPI.InstanceColumns.JR_VERSION + "=?";
            }
            int reviewed = 0;
            int unreviewed = 0;
            try (Cursor instanceCursor = instancesDao.getInstancesCursor(selection, selectionArgs)) {
                int len = instanceCursor.getCount();
                StringBuilder selectionBuf = new StringBuilder(InstanceProviderAPI.InstanceColumns._ID + " IN (");
                selectionArgs = new String[len + 1];
                int i = 0;
                if (instanceCursor.getCount() > 0) {
                    instanceCursor.moveToPosition(-1);
                    while (instanceCursor.moveToNext()) {
                        if (i > 0) {
                            selectionBuf.append(",");
                        }
                        selectionBuf.append("?");
                        selectionArgs[i++] = String.valueOf(instanceCursor.getLong(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                    }
                }


                selectionBuf.append(") AND " + TransferInstance.TRANSFER_STATUS + " =?");
                selection = selectionBuf.toString();
                selectionArgs[i] = TransferInstance.STATUS_FORM_RECEIVE;

                try (Cursor transferCursor = transferDao.getInstancesCursor(null, selection, selectionArgs, null)) {
                    if (transferCursor != null && transferCursor.getCount() > 0) {
                        transferCursor.moveToPosition(-1);
                        while (transferCursor.moveToNext()) {
                            long status = transferCursor.getLong(transferCursor.getColumnIndex(TransferInstance.REVIEW_STATUS));
                            if (status == TransferInstance.STATUS_ACCEPTED || status == TransferInstance.STATUS_REJECTED) {
                                reviewed++;
                            } else {
                                unreviewed++;
                            }
                        }
                    }
                }
            }
            viewHolder.reviewedForms.setText(context.getString(R.string.num_reviewed, String.valueOf(reviewed)));
            viewHolder.unReviewedForms.setText(context.getString(R.string.num_unreviewed, String.valueOf(unreviewed)));
        } else {
            viewHolder.reviewedForms.setVisibility(View.GONE);
            viewHolder.unReviewedForms.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public FormHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_checkbox, null);
        return new FormHolder(view);
    }

    public static class FormHolder extends BaseCursorViewHolder {

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvSubtitle)
        TextView tvSubtitle;
        @BindView(R.id.tvReviewForm)
        TextView reviewedForms;
        @BindView(R.id.tvUnReviewForm)
        TextView unReviewedForms;
        @BindView(R.id.checkbox)
        CheckBox checkBox;
        @BindView(R.id.iconfilledform)
        ImageView filledIcon;

        private Form form;

        FormHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public Form getForm() {
            return form;
        }

        public void setForm(Form form) {
            this.form = form;
        }

        public void toggleCheckbox() {
            checkBox.setChecked(!checkBox.isChecked());
        }
    }
}
