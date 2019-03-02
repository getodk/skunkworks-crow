package org.odk.share.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

public class WifiConnectionNotifier {

    private final WifiBroadcastReceiver broadcastReceiver;
    private boolean isReceiverRegistered = false;

    public WifiConnectionNotifier(Context context, WifiConnectionListener listener) {
        broadcastReceiver = new WifiBroadcastReceiver(context, listener);
    }

    public void start() {
        broadcastReceiver.register();
    }

    public void stop() {
        broadcastReceiver.unregister();
    }

    public interface WifiConnectionListener {
        void onWifiStateToggle(boolean isEnabled);
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        private final IntentFilter intentFilter;
        private final Context context;
        private final WifiConnectionListener listener;

        private WifiBroadcastReceiver(Context context, WifiConnectionListener listener) {
            this.context = context;
            this.listener = listener;
            intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        }

        private void register() {
            context.registerReceiver(this, intentFilter);
            isReceiverRegistered = true;
        }

        private void unregister() {
            if (isReceiverRegistered) {
                context.unregisterReceiver(this);
                isReceiverRegistered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        listener.onWifiStateToggle(false);
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        listener.onWifiStateToggle(true);
                        break;
                }
            }
        }
    }
}
