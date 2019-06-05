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

import java.util.LinkedHashSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

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
        holder.subtitle.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT)));
        long id = cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
        holder.checkBox.setChecked(selectedInstances.contains(id));
        holder.reviewedForms.setVisibility(View.GONE);
        holder.unReviewedForms.setVisibility(View.GONE);

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
