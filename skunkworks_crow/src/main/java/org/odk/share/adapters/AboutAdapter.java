package org.odk.share.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.listeners.OnItemClickListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 8/1/2018.
 */

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {
    private Context context;
    private int [][]listItems;
    private final OnItemClickListener listener;

    public AboutAdapter(Context context, int [][]listItems, OnItemClickListener listener) {
        this.context = context;
        this.listItems = listItems;
        this.listener = listener;
    }

    @Override
    public AboutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.about_list_item, null);
        return new AboutAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AboutAdapter.ViewHolder holder, int position) {
        holder.title.setText(context.getString(listItems[position][0]));
        holder.icon.setImageDrawable(context.getResources().getDrawable(listItems[position][1]));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(v, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return listItems.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) public TextView title;
        @BindView(R.id.ivIcon) public ImageView icon;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
