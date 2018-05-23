package org.odk.share.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.adapters.WifiResultAdapter;
import org.odk.share.controller.Wifi;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

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

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        scanResultList = new ArrayList<>();
        wifiResultAdapter = new WifiResultAdapter(this, scanResultList, this::onListItemClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(wifiResultAdapter);
    }

    @Override
    protected void onResume() {
        startScan();
        registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(receiver);
            unregisterReceiver(wifiStateReceiver);
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }
    }

    private void onListItemClick(View view, int i) {
        Timber.d("Clicked " + scanResultList.get(i));
    }

    public void startScan() {
        scanResultList.clear();
        wifiResultAdapter.notifyDataSetChanged();
        setEmptyViewVisibility(getString(R.string.scanning));
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            scanResultList.addAll(wifiManager.getScanResults());
            unregisterReceiver(this);
            wifiResultAdapter.notifyDataSetChanged();
            setEmptyViewVisibility(getString(R.string.no_wifi_available));
        }
    };

    private void setEmptyViewVisibility(String text) {
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
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    scanResultList.clear();
                    setEmptyViewVisibility(getString(R.string.enable_wifi));
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    startScan();
                    break;
            }
        }
    };

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
                    Toast.makeText(this, getString(R.string.enable_wifi), Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.menu_qr_code:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
