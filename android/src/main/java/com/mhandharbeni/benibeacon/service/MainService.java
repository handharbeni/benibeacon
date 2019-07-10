package com.mhandharbeni.benibeacon.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.recognition.utils.MacAddress;
import com.facebook.react.bridge.Promise;
import com.mhandharbeni.benibeacon.R;
import com.mhandharbeni.benibeacon.RNBenibeaconModule;
import com.mhandharbeni.benibeacon.utils.Constant;
import com.mhandharbeni.benibeacon.utils.NotificationHelper;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.altbeacon.beacon.service.BeaconService.TAG;

public class MainService extends Service implements BeaconConsumer {
    public static boolean isRunning = false;
    public static String FOREGROUND = "com.mhandharbeni.benibeacon.service.MainService";
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
//
            beaconManagers.getBeaconParsers().clear();
            beaconManagers.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Constant.IBEACON_LAYOUT));
            beaconManagers.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
//
            beaconManagers.setForegroundScanPeriod(2000);
            beaconManagers.setForegroundBetweenScanPeriod(5000);
//
            beaconManagers.setBackgroundScanPeriod(1000L);
            beaconManagers.setBackgroundBetweenScanPeriod(30000L);
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
//        beaconManagers.removeAllMonitorNotifiers();
//        beaconManagers.removeAllRangeNotifiers();
//        beaconManagers.addRangeNotifier((collection, region) -> {
//            updateNotification(String.format("%d Beacon In Range", Constant.getListSNBeacon().size()));
//
//            isStartRange = true;
//            Constant.setListNativeBeacon(new ArrayList<>());
//            Constant.setListSNBeacon(new ArrayList<>());
//
//            if (collection.size()<1){
//                if (countAlt < maxCountAlt){
//                    countAlt++;
//                }
//            }else{
//                Constant.setListNativeBeacon(new ArrayList<>());
//                Constant.setListSNBeacon(new ArrayList<>());
//                countAlt = 0;
//            }
//            if (counterUserCoordinate < maxCounterUserCoordinate){
//                counterUserCoordinate++;
//            }
//
//            List<Beacon> listBeacon = new ArrayList<>();
//            listBeacon.clear();
//            for (org.altbeacon.beacon.Beacon beacon:collection){
//                if (beacon.getBluetoothAddress().contentEquals("FF:FF:FF:FF:FF:FF")){
//                    return;
//                }
//                Beacon estimoteBeacon = new com.estimote.coresdk.recognition.packets.Beacon(
//                        UUID.fromString(Constant.BEACON_UUID),
//                        MacAddress.fromString(beacon.getBluetoothAddress()),
//                        0,
//                        0,
//                        beacon.getMeasurementCount(),
//                        beacon.getRssi(),
//                        new Date()
//                );
//                int distance = (int) Math.round(beacon.getDistance());
//                Constant.addEachNativeBeacon(estimoteBeacon);
//                if (distance < 5){
//                    listBeacon.add(estimoteBeacon);
//                }
//            }
//
//            if (counterUserCoordinate >= maxCounterUserCoordinate) {
//                if (listBeacon.size() > 0){
//                    counterUserCoordinate=0;
//                }
//            }
//
//        });
//        beaconManagers.addMonitorNotifier(new MonitorNotifier() {
//            @Override
//            public void didEnterRegion(Region region) {
//                try {
//                    if (!isStartRange){
//                        if (!beaconManagers.isBound(MainService.this)){
//                            beaconManagers.bind(MainService.this);
//                        }
//                        beaconManagers.startRangingBeaconsInRegion(regions);
//                    }
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void didExitRegion(Region region) {
//            }
//
//            @Override
//            public void didDetermineStateForRegion(int i, Region region) {
//                try {
//
//                    switch (i){
//                        case MonitorNotifier.INSIDE :
//                            if (!isStartRange){
//                                beaconManagers.startRangingBeaconsInRegion(regions);
//                            }
//                            break;
//                        case MonitorNotifier.OUTSIDE :
//                            break;
//                    }
//                }catch (Exception e){
//                    Log.d(TAG, "didDetermineStateForRegion: error ");
//                }
//            }
//        });
//
//
//        try {
//            beaconManagers.startMonitoringBeaconsInRegion(regions);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
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

    public BeaconManager getBeaconManagers() {
        return beaconManagers;
    }
}
