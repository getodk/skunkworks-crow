package org.odk.share.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.adapters.WifiResultAdapter;
import org.odk.share.controller.Wifi;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WifiActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.empty_view)
    TextView emptyView;

    private WifiManager wifiManager;
    WifiResultAdapter wifiResultAdapter;
    List<ScanResult> scanResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);
        Wifi wifi = new Wifi(this);

        wifiManager = wifi.getWifiManager();

        if (wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
        }

        scanResultList = new ArrayList<>();
        wifiResultAdapter = new WifiResultAdapter(this, scanResultList, this::onListItemClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(wifiResultAdapter);

        startScan();
        registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    private void onListItemClick(View view, int i) {
        Log.d("Clicked " , scanResultList.get(i) + "");
    }

    public void startScan() {
        scanResultList.clear();
        wifiResultAdapter.notifyDataSetChanged();
        setEmptyViewVisibilty("Scanning.. Wait");
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            scanResultList.addAll(wifiManager.getScanResults());
            unregisterReceiver(this);
            wifiResultAdapter.notifyDataSetChanged();
            setEmptyViewVisibilty("No available wifi. Scan again !");
        }
    };

    private void setEmptyViewVisibilty(String text) {
        if (scanResultList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(text);
        }
    }

    public BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE ,WifiManager.WIFI_STATE_UNKNOWN);

            switch(extraWifiState){
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.d("State","WIFI STATE DISABLED");
                    scanResultList.clear();
                    setEmptyViewVisibilty("Turn on the wifi and then scan.");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d("State","WIFI STATE ENABLED");
                    startScan();
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(receiver);
            unregisterReceiver(wifiStateReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wifi_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                if (wifiManager.isWifiEnabled()) {
                   startScan();
                } else {
                    Toast.makeText(this, "Turn on the wifi to scan", Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.menu_qr_code:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
