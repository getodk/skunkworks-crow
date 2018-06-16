package org.odk.share.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.controller.WifiHotspotHelper;
import org.odk.share.events.HotspotEvent;
import org.odk.share.events.UploadEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;
import org.odk.share.services.HotspotService;
import org.odk.share.services.SenderService;
import org.odk.share.utilities.QRCodeUtils;
import org.odk.share.utilities.SocketUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.VISIBLE;
import static org.odk.share.activities.InstancesList.INSTANCE_IDS;

/**
 * Created by laksh on 6/9/2018.
 */

public class SendActivity extends InjectableActivity {

    public static final String DEFAULT_SSID = "ODK-Share";
    private static final int PROGRESS_DIALOG = 1;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    RxEventBus rxEventBus;
    @Inject
    BaseSchedulerProvider schedulerProvider;
    @Inject
    WifiHotspotHelper wifiHotspot;
    @Inject
    SenderService senderService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvConnectStatus)
    TextView connectStatus;
    @BindView(R.id.ivQRcode)
    ImageView imageQR;
    @BindView(R.id.tvConnectInfo)
    TextView connectInfo;

    private boolean isHotspotRunning;
    private boolean openSettings;
    private ProgressDialog progressDialog;
    private String alertMsg;
    private int port;
    private long[] instancesIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        ButterKnife.bind(this);

        setTitle(getString(R.string.send_forms));
        setSupportActionBar(toolbar);

        instancesIds = getIntent().getLongArrayExtra(INSTANCE_IDS);

        port = SocketUtils.getPort();

        if (port == -1) {
            Timber.e("Port not available for socket communication");
            finish();
        }

        isHotspotRunning = false;
        openSettings = false;

        if (wifiHotspot.isHotspotEnabled()) {
            wifiHotspot.disableHotspot();
        }
        startHotspot();
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
                            break;
                        case DISABLED:
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

        // In devices having Android >= 7, created hotspot having some issues with connecting to other devices.
        // Open settings to trigger the hotspot manually.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            showAlertDialog();
        } else {

            Timber.d("Started hotspot below N");
            wifiHotspot.enableHotspot(DEFAULT_SSID + getString(R.string.hotspot_name_suffix));
            Intent serviceIntent = new Intent(getApplicationContext(), HotspotService.class);
            serviceIntent.setAction(HotspotService.ACTION_START);
            startService(serviceIntent);

            Intent intent = new Intent(getApplicationContext(), HotspotService.class);
            intent.setAction(HotspotService.ACTION_STATUS);
            startService(intent);

            startSending();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.hotspot_settings_dialog);
        builder.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                wifiHotspot.saveLastConfig();
                WifiConfiguration newWifiConfig = wifiHotspot.createNewConfig(DEFAULT_SSID + getString(R.string.hotspot_name_suffix));
                wifiHotspot.setCurrConfig(newWifiConfig);
                wifiHotspot.setWifiConfig(newWifiConfig);
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                openSettings = true;
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (openSettings) {
            openSettings = false;
            Intent serviceIntent = new Intent(getApplicationContext(), HotspotService.class);
            serviceIntent.setAction(HotspotService.ACTION_START);
            startService(serviceIntent);

            Intent intent = new Intent(getApplicationContext(), HotspotService.class);
            intent.setAction(HotspotService.ACTION_STATUS);
            startService(intent);
            Timber.d("Started hotspot N");

            startSending();
        }

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
                            createAlertDialog(getString(R.string.transfer_result), getString(R.string.send_success, result));
                            break;
                        case ERROR:
                            Toast.makeText(this, R.string.error_while_downloading, Toast.LENGTH_SHORT).show();
                            hideDialog(PROGRESS_DIALOG);
                            break;
                        case CANCELLED:
                            Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
                            hideDialog(PROGRESS_DIALOG);
                            break;
                    }
                }, Timber::e);
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

    @Override
    protected void onDestroy() {
        if (isHotspotRunning) {
            wifiHotspot.disableHotspot();
        }

        senderService.cancel();
        Timber.d("Hotspot Stopped");
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void startSending() {
        String ssid = wifiHotspot.getCurrConfig().SSID;

        Timber.d("SSID " + ssid + " " + port);
        Disposable disposable = QRCodeUtils.generateQRCode(ssid, port, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    imageQR.setVisibility(VISIBLE);
                    imageQR.setImageBitmap(bitmap);
                }, Timber::e);
        compositeDisposable.add(disposable);
        connectInfo.setText(getString(R.string.connection_info, String.valueOf(port), ssid));

        senderService.startUploading(instancesIds, port);
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
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                senderService.cancel();
                                dialog.dismiss();
                                finish();
                            }
                        });
                return progressDialog;
        }

        return null;
    }

    private void createAlertDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
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
}
