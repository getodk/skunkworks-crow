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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.views.listeners.ItemClickListener;
import org.odk.share.views.ui.common.basecursor.BaseCursorViewHolder;
import org.odk.share.views.ui.common.basecursor.CursorRecyclerViewAdapter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

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
        viewHolder.filledIcon.setImageResource(R.drawable.ic_blank_form);

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

            cursor = instancesDao.getInstancesCursor(selection, selectionArgs);
            HashMap<Long, Instance> instanceMap = instancesDao.getMapFromCursor(cursor);

            Cursor transferCursor = transferDao.getReceiveInstancesCursor();
            List<TransferInstance> transferInstances = transferDao.getInstancesFromCursor(transferCursor);
            int receiveCount = 0;
            for (TransferInstance instance : transferInstances) {
                if (instanceMap.containsKey(instance.getInstanceId())) {
                    receiveCount++;
                }
            }

            transferCursor = transferDao.getReviewedInstancesCursor();
            transferInstances = transferDao.getInstancesFromCursor(transferCursor);
            int reviewCount = 0;
            for (TransferInstance instance : transferInstances) {
                if (instanceMap.containsKey(instance.getInstanceId())) {
                    reviewCount++;
                }
            }

            viewHolder.reviewedForms.setText(context.getString(R.string.num_reviewed, String.valueOf(reviewCount)));
            viewHolder.unReviewedForms.setText(context.getString(R.string.num_unreviewed, String.valueOf(receiveCount - reviewCount)));
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
