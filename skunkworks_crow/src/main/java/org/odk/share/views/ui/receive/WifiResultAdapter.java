package org.odk.share.views.ui.receive;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.views.listeners.OnItemClickListener;
import org.odk.share.network.WifiNetworkInfo;
import org.odk.share.views.customui.WifiView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 5/22/2018.
 */

public class WifiResultAdapter extends RecyclerView.Adapter<WifiResultAdapter.WifiHolder> {

    private final Context context;
    private final List<WifiNetworkInfo> wifiNetworkInfoList;
    private final OnItemClickListener listener;

    public WifiResultAdapter(Context context, List<WifiNetworkInfo> wifiNetworkInfoList, OnItemClickListener listener) {
        this.context = context;
        this.wifiNetworkInfoList = wifiNetworkInfoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WifiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_single, null);
        return new WifiHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WifiHolder holder, int position) {
        WifiNetworkInfo wifiNetworkInfo = wifiNetworkInfoList.get(holder.getAdapterPosition());
        holder.title.setText(wifiNetworkInfo.getSsid());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(v, holder.getAdapterPosition()));
        holder.wifiIcon.updateState(wifiNetworkInfo.getSecurityType() != WifiConfiguration.KeyMgmt.NONE, wifiNetworkInfo.getRssi());
    }

    @Override
    public int getItemCount() {
        return wifiNetworkInfoList.size();
    }

    public void setList(List<WifiNetworkInfo> list) {
        wifiNetworkInfoList.clear();
        wifiNetworkInfoList.addAll(list);
        notifyDataSetChanged();
    }

    static class WifiHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle)
        TextView title;
        @BindView(R.id.ivWifi)
        WifiView wifiIcon;

        WifiHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
