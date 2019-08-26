package org.odk.share.views.ui.instance.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by laksh on 5/20/2018.
 */

public class InstanceAdapter extends RecyclerView.Adapter<InstanceAdapter.InstanceHolder> {

    private Cursor cursor;
    private Context context;
    private final OnItemClickListener listener;
    private LinkedHashSet<Long> selectedInstances;

    public InstanceAdapter(Context context, Cursor cursor, OnItemClickListener listener,
                           LinkedHashSet<Long> selectedInstances) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
        this.selectedInstances = selectedInstances;
    }

    @NonNull
    @Override
    public InstanceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_checkbox, null);
        return new InstanceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstanceHolder holder, int position) {
        cursor.moveToPosition(holder.getAdapterPosition());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, holder.getAdapterPosition());
            }
        });
        holder.title.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));

        long lastStatusChangeDate = getCursor().getLong(getCursor().getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
        String status = getCursor().getString(getCursor().getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
        String subtext = getDisplaySubtext(context, status, new Date(lastStatusChangeDate));

        holder.subtitle.setText(subtext);
        long id = cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
        holder.checkBox.setChecked(selectedInstances.contains(id));
        holder.reviewedForms.setVisibility(View.GONE);
        holder.unReviewedForms.setVisibility(View.GONE);

    }

    public static String getDisplaySubtext(Context context, String state, Date date) {
        try {
            if (state == null) {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.saved_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_COMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.finalized_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.sent_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(
                        context.getString(R.string.sending_failed_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return !cursor.isClosed() ? cursor.getCount() : 0;
    }



    public Cursor getCursor() {
        return cursor;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    static class InstanceHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) public TextView title;
        @BindView(R.id.tvSubtitle) public TextView subtitle;
        @BindView(R.id.checkbox) public CheckBox checkBox;
        @BindView(R.id.tvReviewForm)
        TextView reviewedForms;
        @BindView(R.id.tvUnReviewForm)
        TextView unReviewedForms;

        InstanceHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
