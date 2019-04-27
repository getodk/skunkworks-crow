package org.odk.share.views.ui.about;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.share.R;
import org.odk.share.views.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 8/1/2018.
 */

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {
    private Context context;
    private List<AboutItem> aboutItems;
    private final OnItemClickListener listener;

    public void addItem(AboutItem aboutItem) {
        aboutItems.add(aboutItem);
    }

    public void addItems(List<AboutItem> aboutItemList) {
        aboutItems.addAll(aboutItemList);
    }

    public void setItems(List<AboutItem> aboutItemList) {
        aboutItems = aboutItemList;
    }

    public AboutAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        aboutItems = new ArrayList<>();
    }

    public AboutAdapter(Context context, List<AboutItem> aboutItems, OnItemClickListener listener) {
        this.context = context;
        this.aboutItems = aboutItems;
        this.listener = listener;
    }

    @Override
    public AboutAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.about_list_item, null);
        return new AboutAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AboutAdapter.ViewHolder holder, int position) {
        holder.title.setText(context.getString(aboutItems.get(position).getTitle()));
        holder.icon.setImageDrawable(context.getResources().getDrawable(aboutItems.get(position).getIcon()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(v, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return aboutItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle)
        public TextView title;
        @BindView(R.id.ivIcon)
        public ImageView icon;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
