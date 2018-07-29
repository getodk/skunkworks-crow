package org.odk.share.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.dto.TransferInstance;
import org.odk.share.listeners.OnItemClickListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 6/30/2018.
 */

public class TransferInstanceAdapter extends RecyclerView.Adapter<TransferInstanceAdapter.ViewHolder> {

    private Context context;
    private List<TransferInstance> items;
    private LinkedHashSet<Long> selectedInstances;
    private boolean showCheckBox;

    private final OnItemClickListener listener;

    public TransferInstanceAdapter(Context context, List<TransferInstance> objects, OnItemClickListener listener, LinkedHashSet<Long> selectedInstances, boolean showCheckBox) {
        this.context = context;
        items = objects;
        this.listener = listener;
        this.selectedInstances = selectedInstances;
        this.showCheckBox = showCheckBox;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) public TextView title;
        @BindView(R.id.tvSubtitle) public TextView subtitle;
        @BindView(R.id.checkbox) public CheckBox checkBox;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        void bind(int position, final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(v, position));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_checkbox, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position, listener);
        TransferInstance instance = items.get(position);
        holder.title.setText(instance.getInstance().getDisplayName());

        if (showCheckBox) {
            holder.checkBox.setVisibility(View.VISIBLE);
            if (selectedInstances.contains(instance.getId())) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        Date date = new Date(instance.getLastStatusChangeDate());
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_at_time),
                Locale.getDefault());
        String statusChangeTime = dateFormat.format(date);

        if (instance.getTransferStatus().equalsIgnoreCase(context.getString(R.string.sent))) {
            holder.subtitle.setText(context.getString(R.string.sent_on, statusChangeTime));
        } else if (instance.getReviewed() == TransferInstance.STATUS_ACCEPTED) {
            holder.subtitle.setText(context.getString(R.string.approved_on, statusChangeTime));
        } else if (instance.getReviewed() == TransferInstance.STATUS_REJECTED) {
            holder.subtitle.setText(context.getString(R.string.rejected_on, statusChangeTime));
        } else {
            holder.subtitle.setText(context.getString(R.string.received_on, statusChangeTime));
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}