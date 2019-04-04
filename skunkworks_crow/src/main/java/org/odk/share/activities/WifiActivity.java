package org.odk.share.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.share.R;
import org.odk.share.adapters.WifiResultAdapter;
import org.odk.share.events.DownloadEvent;
import org.odk.share.listeners.OnItemClickListener;
import org.odk.share.network.WifiConnector;
import org.odk.share.network.WifiNetworkInfo;
import org.odk.share.network.listeners.WifiStateListener;
import org.odk.share.network.receivers.WifiStateBroadcastReceiver;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.services.ReceiverService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class WifiActivity extends InjectableActivity implements OnItemClickListener, WifiStateListener {

    private static final int DIALOG_DOWNLOAD_PROGRESS = 1;
    private static final int DIALOG_CONNECTING = 2;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.wifi_progress_bar)
    ProgressBar wifiProgressBar;
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

    private WifiResultAdapter wifiResultAdapter;
    private List<WifiNetworkInfo> scanResultList;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog = null;
    private String wifiNetworkSSID;
    private WifiInfo lastConnectedWifiInfo;
    private String alertMsg;
    private int port;

    private boolean isQRCodeScanned;
    private String ssidScanned;
    private boolean isProtected;
    private String passwordScanned;

    private WifiConnector wifiConnector;
    private WifiStateBroadcastReceiver wifiStateBroadcastReceiver;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);

        setTitle(getString(R.string.connect_wifi));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isQRCodeScanned = false;
        ssidScanned = null;
        isProtected = false;
        passwordScanned = null;
        lastConnectedWifiInfo = null;
        port = -1;

        scanResultList = new ArrayList<>();
        wifiResultAdapter = new WifiResultAdapter(this, scanResultList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(wifiResultAdapter);

        wifiConnector = new WifiConnector(this);
        wifiStateBroadcastReceiver = new WifiStateBroadcastReceiver(this, this);

        if (!wifiConnector.isWifiEnabled()) {
            wifiConnector.enableWifi();
        } else {
            lastConnectedWifiInfo = wifiConnector.getActiveConnection();
        }
    }

    private boolean isPossibleHotspot(String ssid) {
        return ssid.contains(getString(R.string.hotspot_name_suffix)) ||
                ssid.contains(getString(R.string.hotspot_name_prefix_oreo));
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiStateBroadcastReceiver.register();
        startScan();
        compositeDisposable.add(addDownloadEventSubscription());
    }

    @Override
    protected void onPause() {
        wifiStateBroadcastReceiver.unregister();
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
    }

    @Override
    public void onItemClick(View view, int position) {
        Timber.d("Clicked %s", scanResultList.get(position));

        if (scanResultList.get(position).getSsid().contains(getString(R.string.hotspot_name_prefix_oreo))) {
            Toast.makeText(this, getString(R.string.scan_alert_oreo), Toast.LENGTH_LONG).show();
        } else if (scanResultList.get(position).getSecurityType() != WifiConfiguration.KeyMgmt.NONE) {
            showPasswordDialog(scanResultList.get(position));
        } else {
            connectToNetwork(scanResultList.get(position).getSecurityType(), scanResultList.get(position).getSsid(), null);
        }
    }

    private void connectToNetwork(int securityType, String ssid, String password) {
        alertMsg = getString(R.string.connecting_wifi);
        showDialog(DIALOG_CONNECTING);
        wifiNetworkSSID = ssid;
        wifiConnector.connectToWifi(securityType, wifiNetworkSSID, password);
    }

    private void showPortDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Enter port number")
                .setView(input)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    String portInput = input.getText().toString();
                    Timber.d("Port : %s", portInput);
                    if (portInput.length() > 0) {
                        dialog.dismiss();
                        port = Integer.parseInt(portInput);
                        startReceiveTask();
                    } else {
                        input.setError(getString(R.string.port_empty));
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    isConnected = false;
                    dialog.cancel();
                })
                .create()
                .show();
    }

    private void showPasswordDialog(WifiNetworkInfo scanResult) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.dialog_password, null);
        final EditText passwordEditText = dialogView.findViewById(R.id.etPassword);
        final CheckBox passwordCheckBox = dialogView.findViewById(R.id.checkBox);
        passwordCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!passwordCheckBox.isChecked()) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
        builder.setTitle(scanResult.getSsid());
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.connect), null);
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.setOnShowListener(dialog -> {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String pw = passwordEditText.getText().toString();
                if (!pw.equals("")) {
                    Timber.d(pw);
                    dialog.dismiss();
                    wifiNetworkSSID = scanResult.getSsid();
                    wifiConnector.connectToWifi(scanResult.getSecurityType(), scanResult.getSsid(), pw);
                    removeDialog(DIALOG_CONNECTING);
                } else {
                    passwordEditText.setError(getString(R.string.password_empty));
                }
            });
        });
        alertDialog.show();
    }

    public void startScan() {
        scanWifi.setEnabled(false);
        scanResultList.clear();
        wifiResultAdapter.notifyDataSetChanged();
        setEmptyViewVisibility(getString(R.string.scanning));
        wifiProgressBar.setVisibility(View.VISIBLE);
        wifiConnector.startScan();
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
        wifiProgressBar.setVisibility(View.GONE);
    }

    private void startReceiveTask() {
        showDialog(DIALOG_DOWNLOAD_PROGRESS);
        String dstAddress = wifiConnector.getAccessPointIpAddress();
        receiverService.startDownloading(dstAddress, port);
    }

    @OnClick(R.id.bScan)
    public void scanWifi() {
        startScan();
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
                        (dialog, which) -> {
                            receiverService.cancel();
                            dialog.dismiss();
                            finish();
                        });
                progressDialog.show();
                return progressDialog;
            case DIALOG_CONNECTING:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(alertMsg);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                        (dialog, which) -> {
                            dialog.dismiss();
                            startScan();
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
            wifiConnector.connect(lastConnectedWifiInfo.getNetworkId());
        } else {
            wifiConnector.disableWifi(wifiNetworkSSID);
        }
        super.onDestroy();
    }

    private void createAlertDialog(String title, String message) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), (dialog, i) -> finish());
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
            }
        }
    }

    @Override
    public void onStateUpdate(NetworkInfo.DetailedState detailedState) {

        String connectedSsid = wifiConnector.getWifiSSID();

        Timber.d(wifiNetworkSSID + " " + connectedSsid + " " + detailedState.toString());

        for (WifiNetworkInfo wifiNetworkInfo : scanResultList) {
            if (wifiNetworkInfo.getSsid().equals(connectedSsid) && wifiNetworkInfo.getSsid().equals(wifiNetworkSSID)) {
                wifiNetworkInfo.setState(detailedState);
                wifiResultAdapter.notifyItemChanged(scanResultList.indexOf(wifiNetworkInfo));

                if (!isConnected) {
                    isConnected = true;
                    onConnected();
                }

                break;
            }
        }
    }

    private void onConnected() {
        Toast.makeText(getApplicationContext(), "Connected to " + wifiNetworkSSID, Toast.LENGTH_LONG).show();

        removeDialog(DIALOG_CONNECTING);

        if (port != -1) {
            startReceiveTask();
        } else {
            showPortDialog();
        }
    }

    @Override
    public void onRssiChanged(int rssi) {
        Timber.d(String.valueOf(rssi));
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

        wifiListAvailable(list);
    }

    private void wifiListAvailable(ArrayList<WifiNetworkInfo> list) {
        scanResultList.clear();
        scanResultList.addAll(list);
        wifiResultAdapter.notifyDataSetChanged();

        scanWifi.setEnabled(true);
        setEmptyViewVisibility(getString(R.string.no_wifi_available));

        /*
         * Adding a double verification to check whether the ssid returned by scanned QR Code
         * actually exists or not.
         */
        if (isQRCodeScanned) {
            isQRCodeScanned = false;
            for (WifiNetworkInfo info : list) {
                if (info.getSsid().equals(ssidScanned)) {
                    if (info.getState() != NetworkInfo.DetailedState.CONNECTED) {
                        Toast.makeText(this, "attempting connection", Toast.LENGTH_SHORT).show();
                        connectToNetwork(info.getSecurityType(), ssidScanned, passwordScanned);
                    } else {
                        Toast.makeText(this, "already connected to " + ssidScanned, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }

            Toast.makeText(this, getString(R.string.no_wifi_available), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWifiStateToggle(boolean isEnabled) {
        if (isEnabled) {
            startScan();
        } else {
            scanResultList.clear();
            wifiResultAdapter.notifyDataSetChanged();
            setEmptyViewVisibility(getString(R.string.enable_wifi));
        }
    }
}
