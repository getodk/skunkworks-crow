package org.odk.share.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
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
import org.odk.share.listeners.ProgressListener;
import org.odk.share.tasks.WifiReceiveTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class WifiActivity extends AppCompatActivity implements ProgressListener {

    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.empty_view) TextView emptyView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private WifiManager wifiManager;
    private WifiResultAdapter wifiResultAdapter;
    private List<ScanResult> scanResultList;
    private boolean isReceiverRegistered;
    private boolean isWifiReceiverRegisterd;
    private AlertDialog alertDialog;
    private WifiReceiveTask wifiReceiveTask;

    private ProgressDialog progressDialog = null;
    private static final int DIALOG_DOWNLOAD_PROGRESS = 1;
    private static final int DIALOG_CONNECTING = 2;
    private WifiHelper wifiHelper;
    private String wifiNetworkSSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);

        setTitle(getString(R.string.connect_wifi));
        setSupportActionBar(toolbar);

        wifiHelper = WifiHelper.getInstance(this);

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
            showDialog(DIALOG_CONNECTING);
            wifiNetworkSSID = scanResultList.get(i).SSID;
            wifiHelper.connectToWifi(scanResultList.get(i), null);
            isWifiReceiverRegisterd = true;
            registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
                            dialog.dismiss();
                            wifiNetworkSSID = scanResult.SSID;
                            wifiHelper.connectToWifi(scanResult, pw);
                            isWifiReceiverRegisterd = true;
                            registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                            showDialog(DIALOG_CONNECTING);
                        } else {
                            passwordEditText.setError(getString(R.string.password_empty));
                        }
                    }
                });
            }
        });
        alertDialog.show();
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
                if (scanResult.SSID.contains(getString(R.string.hotspot_name_suffix))) {
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
            Timber.d("RECEIVER CONNECTION ");
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null) {
                    Timber.d(info + " " + info.getTypeName() + " " + info.getType() + " " + wifiNetworkSSID  + " " + isConnected);
                    if (info.getState() == NetworkInfo.State.CONNECTED && info.getTypeName().compareTo("WIFI") == 0) {
                        if (!isConnected && info.getExtraInfo().equals("\"" + wifiNetworkSSID + "\"")) {
                            Timber.d("Connected");
                            isConnected = true;
                            isWifiReceiverRegisterd = false;
                            Toast.makeText(getApplicationContext(), "Connected to " + wifiNetworkSSID, Toast.LENGTH_LONG).show();
                            dismissDialog(DIALOG_CONNECTING);
                            showDialog(DIALOG_DOWNLOAD_PROGRESS);
                            String port = info.getExtraInfo().split("_")[1];
                            String dstAddress = getAccessPointIpAddress(context);
                            wifiReceiveTask = new WifiReceiveTask(dstAddress,
                                    Integer.parseInt(port.substring(0, port.length() - 1)));
                            wifiReceiveTask.setUploaderListener(WifiActivity.this);
                            wifiReceiveTask.execute();
                            unregisterReceiver(this);
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

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Receiving");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);

                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (wifiReceiveTask != null) {
                                    wifiReceiveTask.cancel(true);
                                }
                                dialog.dismiss();
                                finish();
                            }
                        });
                progressDialog.show();
                return progressDialog;
            case DIALOG_CONNECTING:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Connecting to wifi");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }

    public static String getAccessPointIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        byte[] ipAddress = convertToBytes(dhcpInfo.serverAddress);
        try {
            String ip = InetAddress.getByAddress(ipAddress).getHostAddress();
            return ip.replace("/", "");
        } catch (UnknownHostException e) {
            Timber.e(e);
        }
        return null;
    }

    private static byte[] convertToBytes(int hostAddress) {
        return new byte[]{(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
    }

    @Override
    protected void onDestroy() {
        wifiHelper.disableWifi(wifiNetworkSSID);
        super.onDestroy();
    }

    @Override
    public void uploadingComplete(String result) {
        try {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        } catch (Exception e) {
            Timber.e(e);
        }
        Toast.makeText(this, "Files Received", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void progressUpdate(int progress, int total) {
    }

    @Override
    public void onCancel() {
        Toast.makeText(this, " Task Canceled", Toast.LENGTH_LONG).show();
        try {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
