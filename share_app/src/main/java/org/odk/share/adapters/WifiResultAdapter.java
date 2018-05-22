package org.odk.share.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.controller.Wifi;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by laksh on 5/22/2018.
 */

public class WifiResultAdapter extends RecyclerView.Adapter<WifiResultAdapter.WifiHolder>  {

    private Context context;
    private List<ScanResult> wifiScanResult;
    private final OnItemClickListener listener;

    public WifiResultAdapter(Context context, List<ScanResult> wifiScanResult, OnItemClickListener listener) {
        this.context = context;
        this.wifiScanResult = wifiScanResult;
        this.listener = listener;
    }

    @Override
    public WifiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_single, null);
        return new WifiHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WifiHolder holder, int position) {
        holder.title.setText(wifiScanResult.get(holder.getAdapterPosition()).SSID);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, holder.getAdapterPosition());
            }
        });

        if (Wifi.isClose(wifiScanResult.get(holder.getAdapterPosition()))) {
            holder.wifiProtect.setVisibility(VISIBLE);
        } else {
            holder.wifiProtect.setVisibility(GONE);
        }
    }

    @Override
    public int getItemCount() {
        return wifiScanResult.size();
    }

    public class WifiHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) public TextView title;
        @BindView(R.id.ivWifi) public ImageView wifiProtect;

        WifiHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
