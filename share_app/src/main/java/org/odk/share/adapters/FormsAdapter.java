package org.odk.share.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.adapters.baseCursorAdapter.BaseCursorViewHolder;
import org.odk.share.adapters.baseCursorAdapter.CursorRecyclerViewAdapter;
import org.odk.share.adapters.baseCursorAdapter.OnItemClickListener;
import org.odk.share.provider.FormsProviderAPI;

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
        View view = LayoutInflater.from(context).inflate(R.layout.two_item_list, null);
        return new FormHolder(view);
    }

    class FormHolder extends BaseCursorViewHolder {

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvSubtitle)
        TextView tvSubtitle;

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

        }
    }
}
