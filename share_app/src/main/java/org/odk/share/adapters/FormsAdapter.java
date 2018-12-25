package org.odk.share.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.adapters.basecursoradapter.BaseCursorViewHolder;
import org.odk.share.adapters.basecursoradapter.CursorRecyclerViewAdapter;
import org.odk.share.adapters.basecursoradapter.OnItemClickListener;
import org.odk.share.provider.FormsProviderAPI;

import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FormsAdapter extends CursorRecyclerViewAdapter<FormsAdapter.FormHolder> {

    private LinkedHashSet<Long> selectedForms;

    public FormsAdapter(Context context, Cursor cursor, OnItemClickListener listener, LinkedHashSet<Long> selectedForms) {
        super(context, cursor, listener);
        this.selectedForms = selectedForms;
    }

    @Override
    public void onBindViewHolder(FormHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
        String version = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
        String id = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));
        viewHolder.bind(title, version, id, selectedForms == null ? null : selectedForms
                .contains(cursor.getLong(cursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID))));
    }

    @NonNull
    @Override
    public FormHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_checkbox, null);
        return new FormHolder(view);
    }

    class FormHolder extends BaseCursorViewHolder {

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvSubtitle)
        TextView tvSubtitle;
        @BindView(R.id.checkbox)
        public CheckBox checkBox;


        FormHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(String title, String version, String id, Boolean isPresent) {
            tvTitle.setText(title);

            StringBuilder sb = new StringBuilder();
            if (version != null) {
                sb.append(context.getString(R.string.version, version)).append(" ");
            }
            sb.append(context.getString(R.string.id, id));
            tvSubtitle.setText(sb);
            if (isPresent != null) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(isPresent);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, getAdapterPosition());
                    }
                });
            } else {
                checkBox.setVisibility(View.GONE);
            }
        }
    }
}
