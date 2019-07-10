
package com.mhandharbeni.benibeacon;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.estimote.coresdk.recognition.packets.Beacon;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mhandharbeni.benibeacon.service.MainService;
import com.mhandharbeni.benibeacon.utils.Constant;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.RegEx;

public class RNBenibeaconModule extends ReactContextBaseJavaModule {

  public static Exception exception;

  private final ReactApplicationContext reactContext;

  static MainService mService;
  boolean mBound = false;

  public RNBenibeaconModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;

    new BackgroundPowerSaver(getReactApplicationContext());

    BeaconManager.setAndroidLScanningDisabled(true);
    BeaconManager.setUseTrackingCache(true);

  }
  @Override
  public String getName() {
    return "RNBenibeacon";
  }

  @ReactMethod
  public void getListBeacon(Promise promise){
    promise.resolve(getBeacon());
  }

  @ReactMethod
  public void getStateServices(Promise promise){
    promise.resolve(isMyServiceRunning(MainService.class));
  }

  @ReactMethod
  public void startServices(Promise promise){
    try {
      Intent intent = new Intent(MainService.FOREGROUND);
      intent.setClass(this.getReactApplicationContext(), MainService.class);

      new BackgroundPowerSaver(getReactApplicationContext());

      BeaconManager.setAndroidLScanningDisabled(true);
      BeaconManager.setUseTrackingCache(true);

      if (!MainService.isRunning){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          getReactApplicationContext().startForegroundService(intent);
        } else {
          getReactApplicationContext().startService(intent);
        }
      }

      if (exception != null){
          promise.reject(exception);
      }else{
          promise.resolve("success starting services");
      }
    }catch (Exception e){
      promise.reject(e);
    }
  }

  @ReactMethod
  public void forceStartServices(Promise promise){
    try {
      Intent intent = new Intent(MainService.FOREGROUND);
      intent.setClass(this.getReactApplicationContext(), MainService.class);

      new BackgroundPowerSaver(getReactApplicationContext());

      BeaconManager.setAndroidLScanningDisabled(true);
      BeaconManager.setUseTrackingCache(true);

      try{
        getReactApplicationContext().stopService(intent);
      } finally {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          getReactApplicationContext().startForegroundService(intent);
        } else {
          getReactApplicationContext().startService(intent);
        }
      }

      if (exception != null){
        promise.reject(exception);
      }else{
        promise.resolve("success starting services");
      }
    }catch (Exception e){
      promise.reject(e);
    }
  }

  @ReactMethod
  public void bindingService(Promise promise){
    try {
      Intent intent = new Intent(getReactApplicationContext(), MainService.class);
      getReactApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
      promise.resolve("Success Binding Service");

    }catch (Exception e){
      promise.reject(e);
    }
  }

  @ReactMethod
  public void beaconMonitor(){
    if (!mBound) {
      Intent intent = new Intent(getReactApplicationContext(), MainService.class);
      getReactApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    getService().getBeaconManagers().addMonitorNotifier(new MonitorNotifier() {
      @Override
      public void didEnterRegion(Region region) {
        String sRegion = region.getUniqueId();
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("region", sRegion);
        getReactApplicationContext()
                .getJSModule( DeviceEventManagerModule.RCTDeviceEventEmitter.class )
                .emit("enterRegion", writableMap);
      }

      @Override
      public void didExitRegion(Region region) {
        String sRegion = region.getUniqueId();
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("region", sRegion);
        getReactApplicationContext()
                .getJSModule( DeviceEventManagerModule.RCTDeviceEventEmitter.class )
                .emit("exitRegion", writableMap);
      }

      @Override
      public void didDetermineStateForRegion(int i, Region region) {
      }
    });
    getService().startMonitor();
  }

  @ReactMethod
  public void beaconRanging(){
    if (!mBound) {
      Intent intent = new Intent(getReactApplicationContext(), MainService.class);
      getReactApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    getService().getBeaconManagers().addRangeNotifier((collection, region) -> {
      WritableMap writableMap = createRangingResponse(collection, region);
      getReactApplicationContext()
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("onRange", writableMap);
    });
    getService().startRange();
  }

  @ReactMethod
  public void beaconRangingUUID(String regions){
    if (!mBound) {
      Intent intent = new Intent(getReactApplicationContext(), MainService.class);
      getReactApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    getService().getBeaconManagers().addRangeNotifier((collection, region) -> {
      WritableMap writableMap = createRangingResponse(collection, region);
      getReactApplicationContext()
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("onRange", writableMap);
    });

    Region region = new Region("backgroundRegion", Identifier.fromUuid(UUID.fromString(regions)), null, null);
    getService().startRange(region);
  }

  private WritableMap createRangingResponse(Collection<org.altbeacon.beacon.Beacon> beacons, Region region) {
    WritableMap map = new WritableNativeMap();
    map.putString("identifier", region.getUniqueId());
    map.putString("uuid", region.getId1() != null ? region.getId1().toString() : "");
    WritableArray a = new WritableNativeArray();
    for (org.altbeacon.beacon.Beacon beacon : beacons) {
      WritableMap b = new WritableNativeMap();
      b.putString("uuid", beacon.getId1().toString());
      b.putString("macaddress", beacon.getBluetoothAddress());
      b.putInt("major", beacon.getId2().toInt());
      b.putInt("minor", beacon.getId3().toInt());
      b.putInt("rssi", beacon.getRssi());
      b.putDouble("distance", beacon.getDistance());
      a.pushMap(b);
    }
    map.putArray("beacons", a);
    return map;
  }

  private WritableMap getBeacon(){
    WritableMap writableMap = new WritableNativeMap();
    WritableArray writableArray = new WritableNativeArray();


    for (Beacon beacon : Constant.getListSNBeacon()){
      WritableMap b = new WritableNativeMap();
      b.putString("macaddress", beacon.getMacAddress().toString());
      b.putInt("major", beacon.getMajor());
      b.putInt("minor", beacon.getMinor());
      b.putInt("rssi", beacon.getRssi());
      writableArray.pushMap(b);
    }
    writableMap.putArray("beacons", writableArray);
    return writableMap;
  }

  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) reactContext.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  public static void checkPermissions(Activity activity, Context context){
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
    };

    if(!hasPermissions(context, PERMISSIONS)){
      ActivityCompat.requestPermissions( activity, PERMISSIONS, PERMISSION_ALL);
    }
  }

  public static boolean hasPermissions(Context context, String... permissions) {
    if (context != null && permissions != null) {
      for (String permission : permissions) {
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }


  @ReactMethod
  public static MainService getService(){
    return mService;
  }

  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      MainService.LocalBinder binder = (MainService.LocalBinder) service;
      mService = binder.getService();
      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };
}