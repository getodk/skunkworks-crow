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
import org.odk.share.provider.FormsProviderAPI;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.activities.MainActivity.REVIEWED;
import static org.odk.share.activities.MainActivity.UNREVIEWED;

public class FormsAdapter extends CursorRecyclerViewAdapter<FormsAdapter.FormHolder> {

    private Map<String, Map<String, Map<String, Integer>>> formMap;

    public FormsAdapter(Context context, Cursor cursor, OnItemClickListener listener, Map<String, Map<String, Map<String, Integer>>> formMap) {
        super(context, cursor, listener);
        this.formMap = formMap;
    }

    @Override
    public void onBindViewHolder(FormHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
        String version = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
        String id = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));
        int reviewed = 0;
        int unReviewed = 0;
        if (formMap.containsKey(id)) {
            Map<String, Map<String, Integer>> versionMap = formMap.get(id);
            if (versionMap.containsKey(version)) {
                Map<String, Integer> statusMap = versionMap.get(version);
                if (statusMap.containsKey(REVIEWED)) {
                    reviewed = statusMap.get(REVIEWED);
                }
                if (statusMap.containsKey(UNREVIEWED)) {
                    unReviewed = statusMap.get(UNREVIEWED);
                }
            }
        }
        viewHolder.bind(title, version, id, reviewed, unReviewed);
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

        void bind(String title, String version, String id, int numReviewed, int numUnreviewed) {
            tvTitle.setText(title);

            StringBuilder sb = new StringBuilder();
            if (version != null) {
                sb.append(context.getString(R.string.version, version)).append(" ");
            }
            sb.append(context.getString(R.string.id, id));

            tvSubtitle.setText(sb);
            reviewedForms.setText(context.getString(R.string.num_reviewed, String.valueOf(numReviewed)));
            unReviewedForms.setText(context.getString(R.string.num_unreviewed, String.valueOf(numUnreviewed)));

        }
    }
}
