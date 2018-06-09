package org.odk.share.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.controller.WifiHotspotHelper;
import org.odk.share.listeners.ProgressListener;
import org.odk.share.services.HotspotService;
import org.odk.share.tasks.HotspotSendTask;
import org.odk.share.utilities.ArrayUtils;

import java.io.IOException;
import java.net.ServerSocket;

import timber.log.Timber;

import static org.odk.share.activities.InstancesList.INSTANCE_IDS;

public class SendActivity extends AppCompatActivity implements ProgressListener {

    private boolean isHotspotRunning;
    private LocalBroadcastManager localBroadcastManager;
    private WifiHotspotHelper wifiHotspot;
    private boolean openSettings;
    private Long[] instancesToSend;
    private HotspotSendTask hotspotSendTask;

    private static final int PROGRESS_DIALOG = 1;
    private ProgressDialog progressDialog;
    private String alertMsg;
    private ServerSocket serverSocket;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        long[] instancesIds = getIntent().getLongArrayExtra(INSTANCE_IDS);
        instancesToSend = ArrayUtils.toObject(instancesIds);

        wifiHotspot = WifiHotspotHelper.getInstance(this);

        try {
            serverSocket = new ServerSocket(0);
            wifiHotspot.setPort(serverSocket.getLocalPort());
        } catch (IOException e) {
            Timber.e(e);
            finish();
        }

        isHotspotRunning = false;
        openSettings = false;

        if (wifiHotspot.isHotspotEnabled()) {
            wifiHotspot.disableHotspot();
        }
        startHotspot();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
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
            wifiHotspot.enableHotspot();
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
                wifiHotspot.setWifiConfig(wifiHotspot.createNewConfig(WifiHotspotHelper.ssid +
                        getString(R.string.hotspot_name_suffix) + "_" + wifiHotspot.getPort()));
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
        localBroadcastManager.registerReceiver(receiverHotspotEnabled, new IntentFilter(HotspotService.BROADCAST_HOTSPOT_ENABLED));
        localBroadcastManager.registerReceiver(receiverHotspotDisabled, new IntentFilter(HotspotService.BROADCAST_HOTSPOT_DISABLED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        localBroadcastManager.unregisterReceiver(receiverHotspotDisabled);
        localBroadcastManager.unregisterReceiver(receiverHotspotEnabled);
    }

    @Override
    protected void onDestroy() {
        if (isHotspotRunning) {
            wifiHotspot.disableHotspot();
        }

        if (hotspotSendTask != null) {
            hotspotSendTask.cancel(true);
        }
        Timber.d("Hotspot Stopped");
        super.onDestroy();
    }

    private final BroadcastReceiver receiverHotspotDisabled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HotspotService.BROADCAST_HOTSPOT_DISABLED)) {
                isHotspotRunning = false;
            }
        }
    };

    private final BroadcastReceiver receiverHotspotEnabled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HotspotService.BROADCAST_HOTSPOT_ENABLED)) {
                isHotspotRunning = true;
            }
        }
    };

    private void startSending() {
        showDialog(PROGRESS_DIALOG);
        hotspotSendTask = new HotspotSendTask(serverSocket);
        hotspotSendTask.setUploaderListener(this);
        hotspotSendTask.execute(instancesToSend);
    }

    @Override
    public void uploadingComplete(String result) {
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }
        createAlertDialog(getString(R.string.transfer_result), getString(R.string.send_success, result));
    }

    @Override
    public void progressUpdate(int progress, int total) {
        alertMsg = getString(R.string.sending_items, String.valueOf(progress), String.valueOf(total));
        progressDialog.setMessage(alertMsg);
    }

    @Override
    public void onCancel() {
        Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }
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
                        if (hotspotSendTask != null) {
                            hotspotSendTask.cancel(true);
                        }
                        dialog.dismiss();
                        finish();
                    }
                });
                return progressDialog;
        }

        return null;
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
}
