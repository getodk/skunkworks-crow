package org.odk.share.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.share.R;
import org.odk.share.adapters.WifiResultAdapter;
import org.odk.share.controller.WifiHelper;
import org.odk.share.events.DownloadEvent;
import org.odk.share.listeners.OnItemClickListener;
import org.odk.share.network.WifiBroadcastReceiver;
import org.odk.share.network.WifiConnector;
import org.odk.share.network.WifiNetworkInfo;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.services.ReceiverService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.google.zxing.integration.android.IntentIntegrator.QR_CODE_TYPES;
import static org.odk.share.utilities.QRCodeUtils.PASSWORD;
import static org.odk.share.utilities.QRCodeUtils.PORT;
import static org.odk.share.utilities.QRCodeUtils.PROTECTED;
import static org.odk.share.utilities.QRCodeUtils.SSID;

public class WifiActivity extends InjectableActivity implements OnItemClickListener {

    private static final int DIALOG_DOWNLOAD_PROGRESS = 1;
    private static final int DIALOG_CONNECTING = 2;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bScanQRCode)
    Button scanQRCode;
    @BindView(R.id.bScan)
    Button scanWifi;

    @Inject
    ReceiverService receiverService;

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    private WifiManager wifiManager;
    private WifiResultAdapter wifiResultAdapter;
    private List<ScanResult> scanResultList;
    private boolean isReceiverRegistered;
    private boolean isWifiReceiverRegisterd;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog = null;
    private WifiHelper wifiHelper;
    private String wifiNetworkSSID;
    private WifiInfo lastConnectedWifiInfo;
    private String alertMsg;
    private int port;

    public BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        boolean isConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("RECEIVER CONNECTION ");
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null) {
                    Timber.d(info + " " + info.getTypeName() + " " + info.getType() + " " + wifiNetworkSSID + " " + isConnected);
                    if (info.getState() == NetworkInfo.State.CONNECTED
                            && info.getTypeName().compareTo("WIFI") == 0
                            && info.getExtraInfo() != null) {
                        if (!isConnected && info.getExtraInfo().equals("\"" + wifiNetworkSSID + "\"")) {
                            Timber.d("Connected");
                            isConnected = true;
                            isWifiReceiverRegisterd = false;
                            unregisterReceiver(this);
                            Toast.makeText(getApplicationContext(), "Connected to " + wifiNetworkSSID, Toast.LENGTH_LONG).show();
                            removeDialog(DIALOG_CONNECTING);

                            if (port != -1) {
                                startReceiveTask();
                            } else {
                                showPortDialog();
                            }
                        }
                    }
                }
            }
        }
    };

    private boolean isQRCodeScanned;
    private String ssidScanned;
    private boolean isProtected;
    private String passwordScanned;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            for (ScanResult scanResult : wifiManager.getScanResults()) {
                if (scanResult.SSID.contains(getString(R.string.hotspot_name_suffix)) ||
                        scanResult.SSID.contains(getString(R.string.hotspot_name_prefix_oreo))) {
                    scanResultList.add(scanResult);
                }
            }
            isReceiverRegistered = false;
            unregisterReceiver(this);
            wifiResultAdapter.notifyDataSetChanged();
            scanWifi.setEnabled(true);
            setEmptyViewVisibility(getString(R.string.no_wifi_available));


            /*
                Adding a double verification to check whether the ssid returned by scanned QR Code
                actually exists or not.
            */
            if (isQRCodeScanned) {
                isQRCodeScanned = false;
                for (ScanResult scanResult : scanResultList) {
                    if (scanResult.SSID.equals(ssidScanned)) {
                        connectToNetwork(ssidScanned, passwordScanned);
                        return;
                    }
                }

                Toast.makeText(c, getString(R.string.no_wifi_available), Toast.LENGTH_LONG).show();
            }
        }
    };

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

    private WifiConnector wifiConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);

        setTitle(getString(R.string.connect_wifi));
        setSupportActionBar(toolbar);

        isQRCodeScanned = false;
        ssidScanned = null;
        isProtected = false;
        passwordScanned = null;
        lastConnectedWifiInfo = null;
        port = -1;
        wifiHelper = new WifiHelper(this);

        wifiManager = wifiHelper.getWifiManager();

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        } else {
            lastConnectedWifiInfo = wifiManager.getConnectionInfo();
        }

        scanResultList = new ArrayList<>();
        wifiResultAdapter = new WifiResultAdapter(this, scanResultList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(wifiResultAdapter);

        wifiConnector = new WifiConnector(this, new WifiBroadcastReceiver.WifiBroadcastListener() {
            @Override
            public void onStateUpdate(NetworkInfo.DetailedState detailedState) {
                Timber.d(detailedState.toString());
            }

            @Override
            public void onScanResultsAvailable() {
                List<ScanResult> scanResults = wifiConnector.getWifiManager().getScanResults();
                ArrayList<WifiNetworkInfo> list = new ArrayList<>();

                for (ScanResult scanResult : scanResults) {
                    if (isPossibleHotspot(scanResult.SSID)) {
                        WifiNetworkInfo wifiNetworkInfo = new WifiNetworkInfo();
                        wifiNetworkInfo.setSsid(scanResult.SSID);
                        wifiNetworkInfo.setRssi(WifiManager.calculateSignalLevel(scanResult.level, 100));
                        wifiNetworkInfo.setSecurityType(wifiConnector.getScanResultSecurity(scanResult));
                        list.add(wifiNetworkInfo);
                    }
                }

                Timber.d(Arrays.toString(list.toArray()));
            }
        });
    }

    private boolean isPossibleHotspot(String ssid) {
        return ssid.contains(getString(R.string.hotspot_name_suffix)) ||
                ssid.contains(getString(R.string.hotspot_name_prefix_oreo));
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiConnector.registerReceiver();
        startScan();
        registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        compositeDisposable.add(addDownloadEventSubscription());
    }

    @Override
    protected void onPause() {
        wifiConnector.unregisterReceiver();
        compositeDisposable.clear();
        super.onPause();
    }

    private Disposable addDownloadEventSubscription() {
        return rxEventBus.register(DownloadEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .subscribe(downloadEvent -> {
                    switch (downloadEvent.getStatus()) {
                        case QUEUED:
                            Toast.makeText(this, R.string.download_queued, Toast.LENGTH_SHORT).show();
                            break;
                        case DOWNLOADING:
                            int progress = downloadEvent.getCurrentProgress();
                            int total = downloadEvent.getTotalSize();
                            alertMsg = getString(R.string.receiving_items, String.valueOf(progress), String.valueOf(total));
                            progressDialog.setMessage(alertMsg);
                            break;
                        case FINISHED:
                            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                            String result = downloadEvent.getResult();
                            createAlertDialog(getString(R.string.transfer_result), result);
                            break;
                        case ERROR:
                            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                            createAlertDialog(getString(R.string.transfer_result), getString(R.string.error_while_downloading, downloadEvent.getResult()));
                            break;
                        case CANCELLED:
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
                            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                            break;
                    }
                }, Timber::e);
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

    @Override
    public void onItemClick(View view, int position) {
        Timber.d("Clicked %s", scanResultList.get(position));

        if (scanResultList.get(position).SSID.contains(getString(R.string.hotspot_name_prefix_oreo))) {
            Toast.makeText(this, getString(R.string.scan_alert_oreo), Toast.LENGTH_LONG).show();
        } else if (WifiHelper.isClose(scanResultList.get(position))) {
            // Show dialog and ask for password
            showPasswordDialog(scanResultList.get(position));
        } else {
            // connect
            connectToNetwork(scanResultList.get(position).SSID, null);
        }
    }

    private void connectToNetwork(String ssid, String password) {
        alertMsg = getString(R.string.connecting_wifi);
        showDialog(DIALOG_CONNECTING);
        wifiNetworkSSID = ssid;
        wifiHelper.connectToWifi(wifiNetworkSSID, password);
        isWifiReceiverRegisterd = true;
        registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void showPortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter port number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok), null);
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String portInput = input.getText().toString();
                        Timber.d("Port : %s", portInput);
                        if (portInput.length() > 0) {
                            dialog.dismiss();
                            port = Integer.parseInt(portInput);
                            startReceiveTask();
                        } else {
                            input.setError(getString(R.string.port_empty));
                        }
                    }
                });
            }
        });
        alertDialog.show();
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
                            wifiHelper.connectToWifi(scanResult.SSID, pw);
                            isWifiReceiverRegisterd = true;
                            registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                            removeDialog(DIALOG_CONNECTING);
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
        scanWifi.setEnabled(false);
        scanResultList.clear();
        wifiResultAdapter.notifyDataSetChanged();
        setEmptyViewVisibility(getString(R.string.scanning));
        isReceiverRegistered = true;
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

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

    private void startReceiveTask() {
        showDialog(DIALOG_DOWNLOAD_PROGRESS);
        String dstAddress = wifiHelper.getAccessPointIpAddress();
        receiverService.startDownloading(dstAddress, port);
    }

    @OnClick(R.id.bScan)
    public void scanWifi() {
        if (wifiManager.isWifiEnabled()) {
            startScan();
        } else {
            Toast.makeText(this, getString(R.string.enable_wifi), Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.bScanQRCode)
    public void scanQRCode() {
        new IntentIntegrator(this)
                .setPrompt(getString(R.string.scan_qr_code))
                .setDesiredBarcodeFormats(QR_CODE_TYPES)
                .setCameraId(0)
                .setOrientationLocked(false)
                .setCaptureActivity(CaptureActivity.class)
                .setBeepEnabled(true)
                .initiateScan();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(alertMsg);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);

                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                receiverService.cancel();
                                dialog.dismiss();
                                finish();
                            }
                        });
                progressDialog.show();
                return progressDialog;
            case DIALOG_CONNECTING:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(alertMsg);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isWifiReceiverRegisterd = false;
                                unregisterReceiver(wifiReceiver);
                                dialog.dismiss();
                                startScan();
                            }
                        });
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }

    @Override
    protected void onDestroy() {

        if (lastConnectedWifiInfo != null) {
            wifiHelper.getWifiManager().enableNetwork(lastConnectedWifiInfo.getNetworkId(), true);
            wifiHelper.getWifiManager().reconnect();
        } else {
            wifiHelper.disableWifi(wifiNetworkSSID);
        }
        super.onDestroy();
    }

    private void createAlertDialog(String title, String message) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), quitListener);
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // request was canceled...
                Timber.i("QR code scanning cancelled");
            } else {
                Timber.d("RESULT " + result);
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    ssidScanned = (String) obj.get(SSID);
                    port = (Integer) obj.get(PORT);
                    isProtected = (boolean) obj.get(PROTECTED);
                    if (isProtected) {
                        passwordScanned = (String) obj.get(PASSWORD);
                    }

                    Timber.d("Scanned results " + ssidScanned + " " + port + " " + isProtected + " " + passwordScanned);
                    isQRCodeScanned = true;
                    startScan();
                } catch (JSONException e) {
                    Timber.e(e);
                }

                return;
            }
        }
    }
}
