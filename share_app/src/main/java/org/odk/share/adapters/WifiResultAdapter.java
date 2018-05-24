package org.odk.share.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.controller.Wifi;
import org.odk.share.listeners.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        holder.itemView.setOnClickListener(v -> listener.onItemClick(v, holder.getAdapterPosition()));

        int level = WifiManager.calculateSignalLevel(wifiScanResult.get(holder.getAdapterPosition()).level, 4);
        if (Wifi.isClose(wifiScanResult.get(holder.getAdapterPosition()))) {
            switch (level) {
                case 0:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifilock_1));
                    break;
                case 1:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifilock_2));
                    break;
                case 2:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifilock_3));
                    break;
                case 3:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifilock_4));
                    break;
            }
        } else {
            switch (level) {
                case 0:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifi_1));
                    break;
                case 1:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifi_2));
                    break;
                case 2:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifi_3));
                    break;
                case 3:
                    holder.wifiProtect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_signal_wifi_4));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return wifiScanResult.size();
    }

    static class WifiHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) public TextView title;
        @BindView(R.id.ivWifi) public ImageView wifiProtect;

        WifiHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
