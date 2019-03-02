package org.odk.share.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private WifiBroadcastListener listener;

    public WifiBroadcastReceiver(WifiBroadcastListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    listener.onStateUpdate(networkInfo.getDetailedState());
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    listener.onScanResultsAvailable();
                    break;
            }
        }
    }

    public interface WifiBroadcastListener {
        void onStateUpdate(NetworkInfo.DetailedState detailedState);

        void onScanResultsAvailable();
    }
}