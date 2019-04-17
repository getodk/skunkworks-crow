package org.odk.share.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.events.HotspotEvent;
import org.odk.share.events.UploadEvent;
import org.odk.share.network.WifiConnector;
import org.odk.share.network.WifiHospotConnector;
import org.odk.share.network.WifiHotspotManager;
import org.odk.share.preferences.SharedPreferencesHelper;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.services.HotspotService;
import org.odk.share.services.SenderService;
import org.odk.share.utilities.QRCodeUtils;
import org.odk.share.utilities.SocketUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static android.view.View.VISIBLE;
import static org.odk.share.activities.InstancesList.INSTANCE_IDS;
import static org.odk.share.fragments.BlankFormsFragment.FORM_IDS;
import static org.odk.share.fragments.ReviewedInstancesFragment.MODE;
import static org.odk.share.utilities.ApplicationConstants.ASK_REVIEW_MODE;

/**
 * Created by laksh on 6/9/2018.
 */

public class SendActivity extends InjectableActivity {

    public static final String DEFAULT_SSID = "ODK-SKUNKWORKS";
    private static final int PROGRESS_DIALOG = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Inject
    RxEventBus rxEventBus;
    @Inject
    BaseSchedulerProvider schedulerProvider;
    @Inject
    WifiHospotConnector wifiHotspot;
    @Inject
    SenderService senderService;
    @Inject
    SharedPreferencesHelper sharedPreferencesHelper;
    @Inject
    WifiHotspotManager wifiHotspotManager;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvConnectStatus)
    TextView connectStatus;
    @BindView(R.id.ivQRcode)
    ImageView imageQR;
    @BindView(R.id.tvConnectInfo)
    TextView connectInfo;

    private boolean isHotspotInitiated;
    private boolean isHotspotRunning;
    private boolean openSettings;
    private ProgressDialog progressDialog;
    private String alertMsg;
    private int port;
    private long[] formIds;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        ButterKnife.bind(this);

        setTitle(getString(R.string.send_forms));
        setSupportActionBar(toolbar);

        formIds = getIntent().getLongArrayExtra(INSTANCE_IDS);
        mode = getIntent().getIntExtra(MODE, ASK_REVIEW_MODE);
        if (formIds == null) {
            formIds = getIntent().getLongArrayExtra(FORM_IDS);
        }

        port = SocketUtils.getPort();

        if (port == -1) {
            Toast.makeText(this, "Port not available for socket communication", Toast.LENGTH_SHORT).show();
            finish();
        }

        WifiConnector wifiConnector = new WifiConnector(this);
        if (wifiConnector.isWifiEnabled()) {
            wifiConnector.disableWifi(null);
        }

        isHotspotInitiated = false;
        isHotspotRunning = false;
        openSettings = false;

        wifiHotspotManager.stopHotspot();

        //location permission is needed for using hotspot
        checkLocationPermission();
    }

    @Override
    public void onBackPressed() {
        if (!isHotspotRunning) {
            finish();
        } else {
            stopHotspotAlertDialog();
        }
    }

    /**
     * Creates a subscription for listening to all hotspot events being send through the
     * application's {@link RxEventBus}
     */
    private Disposable addHotspotEventSubscription() {
        return rxEventBus.register(HotspotEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .subscribe(hotspotEvent -> {
                    switch (hotspotEvent.getStatus()) {
                        case ENABLED:
                            isHotspotRunning = true;
                            connectStatus.setText(getString(R.string.waiting_connection));
                            rxEventBus.post(new UploadEvent(UploadEvent.Status.QUEUED));
                            break;
                        case DISABLED:
                            connectStatus.setText(getString(R.string.connection_issue_hotspot));
                            isHotspotRunning = false;
                            break;
                    }
                });
    }

    private void startHotspot() {
        if (!isHotspotRunning) {
            initiateHotspot();
        } else {
            Toast.makeText(this, getString(R.string.hotspot_already_running), Toast.LENGTH_LONG).show();
        }
    }

    private void initiateHotspot() {
        // String hotspotName = DEFAULT_SSID + getString(R.string.hotspot_name_suffix);

        try {
            wifiHotspotManager.startHotspot();

            isHotspotInitiated = true;
            startSending();
        } catch (WifiHotspotManager.GpsNotEnabledException e) {
            isHotspotInitiated = false;
            showLocationAlertDialog();
        } catch (WifiHotspotManager.TriggerHotspotException e) {
            showAlertDialog();
        }
    }

    private void showLocationAlertDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.location_settings_dialog)
                .setPositiveButton(getString(R.string.settings), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.hotspot_settings_dialog)
                .setPositiveButton(getString(R.string.settings), (dialog, which) -> {
                    isHotspotInitiated = true;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // do nothing
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wifiHotspot.saveLastConfig();
                        WifiConfiguration newWifiConfig = wifiHotspot.createNewConfig(DEFAULT_SSID + getString(R.string.hotspot_name_suffix));
                        wifiHotspot.setCurrConfig(newWifiConfig);
                        wifiHotspot.setWifiConfig(newWifiConfig);
                    }

                    openTetheringIntent();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void openTetheringIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        openSettings = true;
    }

    private void stopHotspotAlertDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.stop_sending)
                .setPositiveButton(getString(R.string.stop), (dialog, which) -> {
                    stopHotspot();
                    finish();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void stopHotspot() {
        if (isHotspotRunning) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                wifiHotspot.disableHotspot();
            }
        }

        senderService.cancel();
        compositeDisposable.dispose();

        Timber.d("Hotspot Stopped");
    }

    @Override
    protected void onResume() {
        super.onResume();
        compositeDisposable.add(addHotspotEventSubscription());
        compositeDisposable.add(addUploadEventSubscription());
    }

    private Disposable addUploadEventSubscription() {
        return rxEventBus.register(UploadEvent.class)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.androidThread())
                .subscribe(uploadEvent -> {
                    switch (uploadEvent.getStatus()) {
                        case QUEUED:
                            Toast.makeText(this, R.string.upload_queued, Toast.LENGTH_SHORT).show();
                            String ip = new WifiConnector(this).getWifiApIp();
                            String ssid = sharedPreferencesHelper.getHotspotName();
                            String password = sharedPreferencesHelper.getHotspotPassword();
                            setupConnectionInfo(ip, ssid, port, password);
                            break;
                        case UPLOADING:
                            int progress = uploadEvent.getCurrentProgress();
                            int total = uploadEvent.getTotalSize();
                            alertMsg = getString(R.string.sending_items, String.valueOf(progress), String.valueOf(total));
                            setDialogMessage(PROGRESS_DIALOG, alertMsg);
                            break;
                        case FINISHED:
                            hideDialog(PROGRESS_DIALOG);
                            String result = uploadEvent.getResult();
                            createAlertDialog(getString(R.string.transfer_result), result);
                            break;
                        case ERROR:
                            hideDialog(PROGRESS_DIALOG);
                            createAlertDialog(getString(R.string.transfer_result), getString(R.string.error_while_uploading, uploadEvent.getResult()));
                            break;
                        case CANCELLED:
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
                            hideDialog(PROGRESS_DIALOG);
                            break;
                    }
                }, Timber::e);
    }

    private void setupConnectionInfo(String ip, String ssid, int port, String password) {
        Timber.d("setupConnectionInfo() called with: ip = [" + ip + "], ssid = [" + ssid + "], port = [" + port + "], password = [" + password + "]");

        // display connection info
        connectInfo.setText(getString(R.string.connection_info, String.valueOf(port), ssid));

        // setup QR code
        compositeDisposable.add(
                QRCodeUtils.generateQRCode(ip, ssid, port, password)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.androidThread())
                        .subscribe(bitmap -> {
                            imageQR.setVisibility(VISIBLE);
                            imageQR.setImageBitmap(bitmap);
                        }, Timber::e));
    }

    private void setDialogMessage(int dialogId, String message) {
        if (progressDialog == null) {
            showDialog(dialogId);
        }

        progressDialog.setMessage(message);
    }

    private void hideDialog(int dialogId) {
        if (progressDialog != null) {
            dismissDialog(dialogId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        compositeDisposable.clear();
    }

    private void startSending() {
        Intent serviceIntent = new Intent(getApplicationContext(), HotspotService.class);
        serviceIntent.setAction(HotspotService.ACTION_START);
        startService(serviceIntent);

        Intent intent = new Intent(getApplicationContext(), HotspotService.class);
        intent.setAction(HotspotService.ACTION_STATUS);
        startService(intent);

        // required for generating QR Code
        wifiHotspotManager.saveCurrentSettings();

        senderService.startUploading(formIds, port, mode);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle(getString(R.string.waiting_connection));
                progressDialog.setMessage(alertMsg);
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> {
                    senderService.cancel();
                    dialog.dismiss();
                    finish();
                });
                return progressDialog;
        }

        return null;
    }

    private void createAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (dialog, i) -> {
                    stopHotspot();
                    finish();
                })
                .create()
                .show();
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            toggleHotspot();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toggleHotspot();
                } else {
                    Toast.makeText(this, getString(R.string.location_permission_needed), Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
        }
    }

    private void toggleHotspot() {
        if (!isHotspotInitiated) {
            startHotspot();
        } else if (openSettings) {
            openSettings = false;
            startSending();
        }
    }
}
