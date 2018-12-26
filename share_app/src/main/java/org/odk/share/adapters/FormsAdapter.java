package org.odk.share.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.adapters.basecursoradapter.BaseCursorViewHolder;
import org.odk.share.adapters.basecursoradapter.CursorRecyclerViewAdapter;
import org.odk.share.adapters.basecursoradapter.ItemClickListener;
import org.odk.share.dto.Form;
import org.odk.share.provider.FormsProviderAPI;

import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FormsAdapter extends CursorRecyclerViewAdapter<FormsAdapter.FormHolder> {

    @Nullable
    private LinkedHashSet<Long> selectedForms;

    public FormsAdapter(Context context, Cursor cursor, ItemClickListener listener) {
        this(context, cursor, listener, null);
    }

    public FormsAdapter(Context context, Cursor cursor, ItemClickListener listener, @Nullable LinkedHashSet<Long> selectedForms) {
        super(context, cursor, listener);
        this.selectedForms = selectedForms;
    }

    @Override
    public void onBindViewHolder(FormHolder viewHolder, Cursor cursor) {
        long index = cursor.getLong(cursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID));
        String title = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
        String version = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
        String id = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));

        Form form = new Form.Builder()
                .index(index)
                .displayName(title)
                .jrVersion(version)
                .jrFormId(id)
                .build();

        viewHolder.setForm(form);

        viewHolder.tvTitle.setText(title);

        StringBuilder sb = new StringBuilder();
        if (version != null) {
            sb.append(context.getString(R.string.version, version));
        }
        sb.append(context.getString(R.string.id, id));

        viewHolder.tvSubtitle.setText(sb.toString());
        viewHolder.checkBox.setVisibility(selectedForms != null ? View.VISIBLE : View.GONE);
        viewHolder.checkBox.setChecked(selectedForms != null && selectedForms.contains(index));
    }

    @NonNull
    @Override
    public FormHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_checkbox, null);
        return new FormHolder(view);
    }

    public static class FormHolder extends BaseCursorViewHolder {

        @BindView(R.id.tvTitle)
        public TextView tvTitle;
        @BindView(R.id.tvSubtitle)
        public TextView tvSubtitle;
        @BindView(R.id.checkbox)
        public CheckBox checkBox;

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
    }
}
