package org.odk.share.adapters;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 6/30/2018.
 */

public class TransferInstanceAdapter extends RecyclerView.Adapter<TransferInstanceAdapter.ViewHolder> {

    @Nullable
    private final OnItemClickListener listener;
    private final Context context;
    private final List<TransferInstance> items;
    private final LinkedHashSet<Long> selectedInstances;
    private final boolean showCheckBox;

    public TransferInstanceAdapter(Context context, List<TransferInstance> objects,
                                   @Nullable OnItemClickListener listener,
                                   LinkedHashSet<Long> selectedInstances, boolean showCheckBox) {
        this.context = context;
        items = objects;
        this.listener = listener;
        this.selectedInstances = selectedInstances;
        this.showCheckBox = showCheckBox;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(context).inflate(R.layout.list_item_checkbox, parent, false);
        return new ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
            if (instance.getReceivedReviewStatus() == TransferInstance.STATUS_ACCEPTED ||
                    instance.getReceivedReviewStatus() == TransferInstance.STATUS_REJECTED) {
                StringBuilder sb = new StringBuilder();
                sb.append(context.getString(R.string.form_received_by_reviewer,
                        instance.getReceivedReviewStatus() == TransferInstance.STATUS_ACCEPTED ?
                                context.getString(R.string.accepted) : context.getString(R.string.rejected)));
                if (instance.getInstructions() != null && instance.getInstructions().length() > 0) {
                    sb.append(context.getString(R.string.feedback_sent, instance.getInstructions()));
                } else {
                    sb.append(context.getString(R.string.no_feedback_sent));
                }
                holder.subtitle.setText(sb.toString());
            } else {
                holder.subtitle.setText(context.getString(R.string.sent_on, statusChangeTime));
            }
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle)
        TextView title;
        @BindView(R.id.tvSubtitle)
        TextView subtitle;
        @BindView(R.id.checkbox)
        CheckBox checkBox;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        void bind(int position, @Nullable final OnItemClickListener listener) {
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(v, position));
            }
        }
    }
}