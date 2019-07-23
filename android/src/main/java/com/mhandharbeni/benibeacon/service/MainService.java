package com.mhandharbeni.benibeacon.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.mhandharbeni.benibeacon.R;
import com.mhandharbeni.benibeacon.RNBenibeaconModule;
import com.mhandharbeni.benibeacon.utils.Constant;
import com.mhandharbeni.benibeacon.utils.NotificationHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.Collection;

import static org.altbeacon.beacon.service.BeaconService.TAG;

public class MainService extends Service implements BeaconConsumer, RangeNotifier, MonitorNotifier {
    public static boolean isRunning = false;
    public static String FOREGROUND = "com.mhandharbeni.benibeacon.service.MainService";
    public static String ACTION_BROADCAST_ENTERREGION = "NULL";
    public static String ACTION_BROADCAST_EXITREGION= "NULL";
    public static String ACTION_BROADCAST_DETERMINEREGION= "NULL";
    public static String ACTION_BROADCAST_RANGE = "NULL";
    private Notification notification;
    private PendingIntent pendingIntent;
    private Intent notificationIntent;

    private NotificationCompat.Builder builder;

    private org.altbeacon.beacon.BeaconManager beaconManagers;
    private Region regions;
    private int countAlt = 0;
    private int maxCountAlt = 24;

    private static int counterUserCoordinate = 0;
    private static int maxCounterUserCoordinate = 2;

    private NotificationCompat.Builder notifBuilder;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "androidLibrary didEnterRegion: "+region.getId1());
        if (!ACTION_BROADCAST_ENTERREGION.equals("NULL")){
            Intent intent = new Intent();
            intent.setAction(ACTION_BROADCAST_ENTERREGION);
            intent.putExtra("data", (Serializable) region);
            sendBroadcast(intent);
        }
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "androidLibrary didExitRegion: "+region.getId1());
        if (!ACTION_BROADCAST_EXITREGION.equals("NULL")){
            Intent intent = new Intent();
            intent.setAction(ACTION_BROADCAST_EXITREGION);
            intent.putExtra("data", (Serializable) region);
            sendBroadcast(intent);
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.d(TAG, "androidLibrary didDetermineState: "+region.getId1());
        if (!ACTION_BROADCAST_DETERMINEREGION.equals("NULL")){
            Intent intent = new Intent();
            intent.setAction(ACTION_BROADCAST_DETERMINEREGION);
            intent.putExtra("data", (Serializable) region);
            sendBroadcast(intent);
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        Log.d(TAG, "androidLibrary didRangeBeacon: "+collection.size());
        if (!ACTION_BROADCAST_RANGE.equals("NULL")){
            Intent intent = new Intent();
            intent.setAction(ACTION_BROADCAST_RANGE);
            intent.putExtra("data", (Serializable) collection);
            sendBroadcast(intent);
        }
    }

    public class LocalBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }


    @SuppressLint("InvalidWakeLockTag")
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        showNotification();
        isRunning = true;
        return START_STICKY;
    }

    private void updateNotification(String text) {
        NotificationHelper.updateNotification(notifBuilder, String.valueOf(Constant.NOTIF_BEACON_FOREGROUNDID), text);
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        beaconManagers.unbind(this);
        stopForeground(true);
        isRunning = false;
        super.onDestroy();
    }
    public void showNotification(){
        notifBuilder = NotificationHelper.createNotifications(
                getApplicationContext(),
                String.valueOf(Constant.NOTIF_BEACON_FOREGROUNDID),
                getString(R.string.app_name),
                "Beacon Scanner Active",
                0,
                MainService.class
        );
        startForeground(Constant.NOTIF_BEACON_FOREGROUNDID, notifBuilder.build());
        usingAltBeacon();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void usingAltBeacon(){

        try {
            beaconManagers = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(getApplicationContext());
            regions = new Region("backgroundRegion", null, null, null);

            beaconManagers.getBeaconParsers().clear();
            beaconManagers.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Constant.IBEACON_LAYOUT));
            beaconManagers.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

            beaconManagers.setForegroundScanPeriod(2000);
            beaconManagers.setForegroundBetweenScanPeriod(5000);

            beaconManagers.setBackgroundScanPeriod(2000);
            beaconManagers.setBackgroundBetweenScanPeriod(5000);
            beaconManagers.setEnableScheduledScanJobs(false);
            beaconManagers.enableForegroundServiceScanning(notifBuilder.build(), Constant.NOTIF_BEACON_FOREGROUNDID);

            beaconManagers.applySettings();
            if (!beaconManagers.isBound(this)){
                beaconManagers.bind(this);
            }
        }catch (Exception e){
            RNBenibeaconModule.exception = e;
        }
    }


    boolean isStartRange = false;

    @SuppressLint("DefaultLocale")
    @Override
    public void onBeaconServiceConnect() {
        updateNotification("Beacon Service Connected");
        beaconManagers.addMonitorNotifier(this);
        beaconManagers.addRangeNotifier(this);
    }

    public boolean startRange(Region region){
        try {
            beaconManagers.startRangingBeaconsInRegion(region);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean startRange(){
        try {
            beaconManagers.startRangingBeaconsInRegion(regions);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean startMonitor(Region region){
        try {
            beaconManagers.startMonitoringBeaconsInRegion(region);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean startMonitor(){
        try {
            beaconManagers.startMonitoringBeaconsInRegion(regions);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean stopRanging(Region region){
        try {
            beaconManagers.stopRangingBeaconsInRegion(region);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean stopRanging(){
        try {
            beaconManagers.stopRangingBeaconsInRegion(regions);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean stopMonitor(Region region){
        try {
            beaconManagers.stopMonitoringBeaconsInRegion(region);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean stopMonitor(){
        try {
            beaconManagers.stopMonitoringBeaconsInRegion(regions);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public BeaconManager getBeaconManagers() {
        return beaconManagers;
    }
}
