
package com.mhandharbeni.benibeacon;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.estimote.coresdk.recognition.packets.Beacon;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.mhandharbeni.benibeacon.service.CobaServices;
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
  public void showToast(String message){
    Toast.makeText(reactContext, message, Toast.LENGTH_SHORT).show();
  }

  @ReactMethod
  public void getToast(Promise promise){
    promise.resolve("Test Value");
  }

  @ReactMethod
  public void getListBeacon(Promise promise){
    promise.resolve(getBeacon());
  }

  @ReactMethod
  public void getRunningServices(Promise promise){
    promise.resolve(isMyServiceRunning(MainService.class));
  }

  @ReactMethod
  public void startServices(Promise promise){
    try {


      new BackgroundPowerSaver(getReactApplicationContext());

      BeaconManager.setAndroidLScanningDisabled(true);
      BeaconManager.setUseTrackingCache(true);
//    if (!isMyServiceRunning(MainService.class)){
//      getReactApplicationContext().startService(new Intent(getReactApplicationContext(), MainService.class));
//    }

      Intent intent = new Intent(MainService.FOREGROUND);
      intent.setClass(this.getReactApplicationContext(), MainService.class);
      getReactApplicationContext().startService(intent);
      if (exception != null){
          promise.reject(exception);
      }else{
          promise.resolve("success");
      }
//      promise.resolve(exception.getMessage());
    }catch (Exception e){
      promise.reject(e);
    }
  }

  @ReactMethod
  public void startCobaServices(){
      Intent intent = new Intent(CobaServices.FOREGROUND);
      intent.setClass(this.getReactApplicationContext(), CobaServices.class);
      getReactApplicationContext().startService(intent);
//      getReactApplicationContext().startService(new Intent(getReactApplicationContext(), CobaServices.class));
  }

  private WritableMap getBeacon(){
    WritableMap writableMap = new WritableNativeMap();
    WritableArray writableArray = new WritableNativeArray();

    WritableMap b = new WritableNativeMap();
    b.putString("macaddress", "Test");
    b.putInt("major", 1);
    b.putInt("minor", 1);
    b.putInt("rssi", 1);
    writableArray.pushMap(b);

    for (Beacon beacon : Constant.getListSNBeacon()){
      b = new WritableNativeMap();
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

//    private void location() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (getReactApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    || getReactApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    || getReactApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                    || getReactApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
//                    || getReactApplicationContext().checkSelfPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED
//                    || getReactApplicationContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
//                    || getReactApplicationContext().checkSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED
//                    || getReactApplicationContext().checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED
//                    /*|| this.checkSelfPermission(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND) != PackageManager.PERMISSION_GRANTED
//                    || this.checkSelfPermission(Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND) != PackageManager.PERMISSION_GRANTED*/){
//                getReactApplicationContext().requestPermissions(new String[]{
//                                Manifest.permission.ACCESS_FINE_LOCATION,
//                                Manifest.permission.ACCESS_COARSE_LOCATION,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.CAMERA,
//                                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
//                                Manifest.permission.READ_PHONE_STATE,
//                                Manifest.permission.RECEIVE_BOOT_COMPLETED,
//                                Manifest.permission.FOREGROUND_SERVICE
//                                /*Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
//                                Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND*/
//                        },
//                        REQUEST_ENABLE_LOC);
//            }
//            else {
//
//                LocationManager locationManager = (LocationManager) getReactApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//                boolean isGpsProviderEnabled, isNetworkProviderEnabled;
//                assert locationManager != null;
//                isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//                if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(getReactApplicationContext());
//                    builder.setTitle("Izin Lokasi");
//                    builder.setMessage("Aplikasi ini membutuhkan izin lokasi. Izinkan lokasi untuk terus menggunakan fitur aplikasi.");
//                    builder.setPositiveButton("IZINKAN", (dialogInterface, i) -> {
//                        Intent intentRedirectionGPSSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        intentRedirectionGPSSettings.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                        getReactApplicationContext().startActivityForResult(intentRedirectionGPSSettings, REQUEST_LOC);
//                    });
//                    builder.setNegativeButton("TOLAK", null);
//                    builder.show();
//                }
//            }
//        }
//    }
}