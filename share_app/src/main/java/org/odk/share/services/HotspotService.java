package org.odk.share.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.odk.share.R;
import org.odk.share.activities.MainActivity;
import org.odk.share.application.Share;
import org.odk.share.controller.WifiHotspotHelper;
import org.odk.share.events.HotspotEvent;
import org.odk.share.rx.RxEventBus;
import org.odk.share.rx.schedulers.BaseSchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by laksh on 5/18/2018.
 */

public class HotspotService extends Service {

    public static final String ACTION_START = "hotspot_start";
    public static final String ACTION_STOP = "hotspot_stop";
    public static final String ACTION_STATUS = "hotspot_status";
    private static final int notify_stop = 5000;
    private static final int notify_start = 1000;
    private static final int HOTSPOT_NOTIFICATION_ID = 34567;
    private static final int START = 1;
    private static final int STATUS = 2;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    RxEventBus rxEventBus;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    private WifiHotspotHelper wifiHotspotHelper;
    private HotspotState state;
    private BroadcastReceiver stopReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // inject dependencies
        ((Share) getApplication()).getAppComponent().inject(this);

        wifiHotspotHelper = WifiHotspotHelper.getInstance(getApplicationContext());
        stopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals(ACTION_STOP)) {
                    stopHotspot();
                }
            }
        };
        state = new HotspotState(this);
        registerReceiver(stopReceiver, new IntentFilter(ACTION_STOP));
        startForeground(HOTSPOT_NOTIFICATION_ID, buildForegroundNotification(getString(R.string.hotspot_start), false));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_START:
                state.sendEmptyMessage(START);
                break;
            case ACTION_STOP:
                stopHotspot();
                break;
            case ACTION_STATUS:
                if (state == null) {
                    state = new HotspotState(this);
                } else {
                    state.removeMessages(STATUS);
                }
                state.sendEmptyMessageDelayed(STATUS, notify_stop);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        if (stopReceiver != null) {
            unregisterReceiver(stopReceiver);
        }
        super.onDestroy();
    }

    private void stopHotspot() {

        if (wifiHotspotHelper != null) {
            wifiHotspotHelper.disableHotspot();
        }

        if (state != null) {
            state.removeCallbacksAndMessages(null);
        }
        stopForeground(true);
        stopSelf();

        rxEventBus.post(new HotspotEvent(HotspotEvent.Status.DISABLED));
    }

    private Notification buildForegroundNotification(String status, boolean showStopButton) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setContentTitle("ODK Share").setContentText(status);
        Intent targetIntent = new Intent(this, MainActivity.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        b.setContentIntent(contentIntent).setSmallIcon(R.mipmap.ic_launcher).setWhen(System.currentTimeMillis());
        if (showStopButton) {
            Intent stopIntent = new Intent(ACTION_STOP);
            PendingIntent stopHotspot = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            b.addAction(R.drawable.ic_close, getString(R.string.stop), stopHotspot);
        }
        return (b.build());
    }

    private void updateNotification(String status, boolean stopAction) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(HOTSPOT_NOTIFICATION_ID, buildForegroundNotification(status, stopAction));
    }

    class HotspotState extends Handler {

        HotspotService service;

        HotspotState(HotspotService service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            if (id == START) {
                if (service != null && service.wifiHotspotHelper != null) {
                    if (service.wifiHotspotHelper.isHotspotEnabled()) {
                        updateNotification(getString(R.string.hotspot_running), true);
                        rxEventBus.post(new HotspotEvent(HotspotEvent.Status.ENABLED));
                    } else {
                        removeMessages(START);
                        sendEmptyMessageDelayed(START, notify_start);
                    }
                }
            } else if (id == STATUS) {
                if (service.wifiHotspotHelper == null || !service.wifiHotspotHelper.isHotspotEnabled()) {
                    stopHotspot();
                } else {
                    sendEmptyMessageDelayed(STATUS, notify_stop);
                }
            }
        }
    }
}