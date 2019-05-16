package com.mhandharbeni.benibeacon.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.recognition.utils.MacAddress;
import com.mhandharbeni.benibeacon.R;
import com.mhandharbeni.benibeacon.RNBenibeaconModule;
import com.mhandharbeni.benibeacon.utils.Constant;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.altbeacon.beacon.service.BeaconService.TAG;

public class MainService extends Service implements BeaconConsumer {
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


    @SuppressLint("InvalidWakeLockTag")
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "Beacon Service onStartCommand: Starting");
// Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.READ_CONTACTS)) {
//
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        showNotification();
        return START_STICKY;

    }

    private Notification getCompatNotification() {
        builder = new NotificationCompat.Builder(this);
        String str = "Scanning Beacon Started";
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Beacon Scanning")
                .setContentText(str)
                .setTicker(str)
                .setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(getApplicationContext(), CobaServices.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000, startIntent, 0);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    private Notification getCurrentNotification(){
        return builder.build();
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        beaconManagers.unbind(this);
        stopForeground(true);
        super.onDestroy();
    }
    public void showNotification(){
        startForeground(Constant.NOTIF_BEACON_FOREGROUNDID, getCompatNotification());
        usingAltBeacon();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
            beaconManagers.enableForegroundServiceScanning(getCompatNotification(), Constant.NOTIF_BEACON_FOREGROUNDID);

            beaconManagers.applySettings();
            beaconManagers.bind(this);
        }catch (Exception e){
            RNBenibeaconModule.exception = e;
        }
    }


    boolean isStartRange = false;

    @Override
    public void onBeaconServiceConnect() {
        beaconManagers.removeAllMonitorNotifiers();
        beaconManagers.removeAllRangeNotifiers();
        beaconManagers.addRangeNotifier((collection, region) -> {
            isStartRange = true;
            Constant.setListNativeBeacon(new ArrayList<>());
            Constant.setListSNBeacon(new ArrayList<>());

            if (collection.size()<1){

                if (countAlt < maxCountAlt){
                    countAlt++;
                }else{

                }
            }else{
                Constant.setListNativeBeacon(new ArrayList<>());
                Constant.setListSNBeacon(new ArrayList<>());
                countAlt = 0;
            }
            if (counterUserCoordinate < maxCounterUserCoordinate){
                counterUserCoordinate++;
            }

            List<Beacon> listBeacon = new ArrayList<>();
            listBeacon.clear();
            for (org.altbeacon.beacon.Beacon beacon:collection){
                if (beacon.getBluetoothAddress().contentEquals("FF:FF:FF:FF:FF:FF")){
                    return;
                }
                Beacon estimoteBeacon = new com.estimote.coresdk.recognition.packets.Beacon(
                        UUID.fromString(Constant.BEACON_UUID),
                        MacAddress.fromString(beacon.getBluetoothAddress()),
                        0,
                        0,
                        beacon.getMeasurementCount(),
                        beacon.getRssi(),
                        new Date()
                );
                int distance = (int) Math.round(beacon.getDistance());
                Constant.addEachNativeBeacon(estimoteBeacon);
                if (distance < 5){
                    listBeacon.add(estimoteBeacon);
                }
            }
            if (counterUserCoordinate >= maxCounterUserCoordinate) {
                if (listBeacon.size() > 0){
                    counterUserCoordinate=0;
                }
            }

        });
        beaconManagers.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    if (!isStartRange){
                        if (!beaconManagers.isBound(MainService.this)){
                            beaconManagers.bind(MainService.this);
                        }
                        beaconManagers.startRangingBeaconsInRegion(regions);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                try {

                    switch (i){
                        case MonitorNotifier.INSIDE :
                            if (!isStartRange){
                                beaconManagers.startRangingBeaconsInRegion(regions);
                            }
                            break;
                        case MonitorNotifier.OUTSIDE :
                            break;
                    }
                }catch (Exception e){
                    Log.d(TAG, "didDetermineStateForRegion: error ");
                }
            }
        });


        try {
            beaconManagers.startMonitoringBeaconsInRegion(regions);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
