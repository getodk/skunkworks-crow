package org.odk.share.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.adapters.WifiResultAdapter;
import org.odk.share.controller.WifiHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class WifiActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.empty_view) TextView emptyView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private WifiManager wifiManager;
    private WifiResultAdapter wifiResultAdapter;
    private List<ScanResult> scanResultList;
    private boolean isReceiverRegistered;
    private boolean isWifiReceiverRegisterd;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);

        setTitle(getString(R.string.view_wifi));
        setSupportActionBar(toolbar);

        WifiHelper wifiHelper = new WifiHelper(this);

        wifiManager = wifiHelper.getWifiManager();

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

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        try {
            unregisterReceiver(wifiStateReceiver);
            if (isReceiverRegistered) {
                unregisterReceiver(receiver);
            }

            if (isWifiReceiverRegisterd) {
                unregisterReceiver(wifiReceiver);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }
    }

    private void onListItemClick(View view, int i) {
        Timber.d("Clicked " + scanResultList.get(i));
        if (WifiHelper.isClose(scanResultList.get(i))) {
            // Show dialog and ask for password
            showPasswordDialog(scanResultList.get(i));
        } else {
            // connect
            connectToWifi(scanResultList.get(i), null);
        }
    }

    private void showPasswordDialog(ScanResult scanResult) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.dialog_password, null);
        final EditText passwordEditText = dialogView.findViewById(R.id.etPassword);
        final CheckBox passwordCheckBox = dialogView.findViewById(R.id.checkBox);
        passwordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!passwordCheckBox.isChecked()) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
        builder.setTitle(scanResult.SSID);
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.connect), null);
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String pw = passwordEditText.getText().toString();
                        if (!pw.equals("")) {
                            Timber.d(pw);
                            connectToWifi(scanResult, pw);
                            dialog.dismiss();
                        } else {
                            passwordEditText.setError(getString(R.string.password_empty));
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void connectToWifi(ScanResult wifiNetwork, String password) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo.getBSSID() != null && wifiInfo.getBSSID().equals(wifiNetwork.BSSID)) {
            // already connected to a network
            Toast.makeText(this, getString(R.string.already_connected) + wifiNetwork.SSID, Toast.LENGTH_LONG).show();
        } else {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + wifiNetwork.SSID + "\"";

            if (WifiHelper.isClose(wifiNetwork)) {
                conf.preSharedKey = "\"" + password + "\"";
            } else {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }

            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                Timber.d(i.SSID);
                if (i.SSID != null && i.SSID.equals("\"" + wifiNetwork.SSID + "\"")) {
                    Timber.d("Found");
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    isWifiReceiverRegisterd = true;
                    registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                    break;
                }
            }
        }
    }

    public void startScan() {
        scanResultList.clear();
        wifiResultAdapter.notifyDataSetChanged();
        setEmptyViewVisibility(getString(R.string.scanning));
        isReceiverRegistered = true;
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            for (ScanResult scanResult: wifiManager.getScanResults()) {
                if (scanResult.SSID.endsWith(getString(R.string.hotspot_name_suffix))) {
                    scanResultList.add(scanResult);
                }
            }
            isReceiverRegistered = false;
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

    public BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        boolean isConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            if (!isConnected) {
                                isConnected = true;
                                isWifiReceiverRegisterd = false;
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                Timber.d(String.valueOf(wifiInfo));
                                Toast.makeText(WifiActivity.this, getString(R.string.connected), Toast.LENGTH_LONG).show();
                                unregisterReceiver(this);
                            }
                        }
                    }
                }
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
