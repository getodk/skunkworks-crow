package org.odk.skunkworks_crow.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.odk.skunkworks_crow.R;
import org.odk.skunkworks_crow.controller.WifiHelper;
import org.odk.skunkworks_crow.listeners.OnItemClickListener;
import org.odk.skunkworks_crow.views.WifiView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 5/22/2018.
 */

public class WifiResultAdapter extends RecyclerView.Adapter<WifiResultAdapter.WifiHolder> {

    private final Context context;
    private final List<ScanResult> wifiScanResult;
    private final OnItemClickListener listener;

    public WifiResultAdapter(Context context, List<ScanResult> wifiScanResult, OnItemClickListener listener) {
        this.context = context;
        this.wifiScanResult = wifiScanResult;
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
        holder.title.setText(wifiScanResult.get(holder.getAdapterPosition()).SSID);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(v, holder.getAdapterPosition()));
        holder.wifiIcon.updateState(WifiHelper.isClose(wifiScanResult.get(holder.getAdapterPosition())),
                wifiScanResult.get(holder.getAdapterPosition()).level);
    }

    @Override
    public int getItemCount() {
        return wifiScanResult.size();
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
