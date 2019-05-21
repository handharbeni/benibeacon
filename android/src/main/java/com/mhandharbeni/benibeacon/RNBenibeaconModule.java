
package com.mhandharbeni.benibeacon;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.estimote.coresdk.recognition.packets.Beacon;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.mhandharbeni.benibeacon.service.MainService;
import com.mhandharbeni.benibeacon.utils.Constant;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

public class RNBenibeaconModule extends ReactContextBaseJavaModule {

  public static Exception exception;

  private final ReactApplicationContext reactContext;

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
}