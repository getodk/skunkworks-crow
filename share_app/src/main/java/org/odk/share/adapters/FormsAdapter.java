package org.odk.share.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.adapters.basecursoradapter.BaseCursorViewHolder;
import org.odk.share.adapters.basecursoradapter.CursorRecyclerViewAdapter;
import org.odk.share.adapters.basecursoradapter.OnItemClickListener;
import org.odk.share.dao.InstancesDao;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.provider.InstanceProviderAPI;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FormsAdapter extends CursorRecyclerViewAdapter<FormsAdapter.FormHolder> {

    public FormsAdapter(Context context, Cursor cursor, OnItemClickListener listener) {
        super(context, cursor, listener);
    }

    @Override
    public void onBindViewHolder(FormHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
        String version = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
        String id = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));
        viewHolder.bind(title, version, id);
    }

    @NonNull
    @Override
    public FormHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.form_item_list, null);
        return new FormHolder(view);
    }

    class FormHolder extends BaseCursorViewHolder {

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvSubtitle)
        TextView tvSubtitle;
        @BindView(R.id.tvReviewForm)
        TextView reviewedForms;
        @BindView(R.id.tvUnReviewForm)
        TextView unReviewedForms;

        FormHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(String title, String version, String id) {
            tvTitle.setText(title);

            StringBuilder sb = new StringBuilder();
            if (version != null) {
                sb.append(context.getString(R.string.version, version)).append(" ");
            }
            sb.append(context.getString(R.string.id, id));

            tvSubtitle.setText(sb);

            String[] selectionArgs;
            String selection;

            if (version == null) {
                selectionArgs = new String[]{id};
                selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                        + InstanceProviderAPI.InstanceColumns.JR_VERSION + " IS NULL";
            } else {
                selectionArgs = new String[]{id, version};
                selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                        + InstanceProviderAPI.InstanceColumns.JR_VERSION + "=?";
            }
            int reviewed = 0;
            int unreviewed = 0;
            try (Cursor instanceCursor = new InstancesDao().getInstancesCursor(selection, selectionArgs)) {
                int len = instanceCursor.getCount();
                StringBuilder selectionBuf = new StringBuilder(InstanceProviderAPI.InstanceColumns._ID + " IN (");
                selectionArgs = new String[len + 1];
                int i = 0;
                if (instanceCursor != null && instanceCursor.getCount() > 0) {
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

                try (Cursor transferCursor = new TransferDao().getInstancesCursor(null, selection, selectionArgs, null)) {
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
            reviewedForms.setText(context.getString(R.string.num_reviewed, String.valueOf(reviewed)));
            unReviewedForms.setText(context.getString(R.string.num_unreviewed, String.valueOf(unreviewed)));
        }
    }
}
