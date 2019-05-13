
package com.mhandharbeni.benibeacon;

import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class RNBenibeaconModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNBenibeaconModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNBenibeacon";
  }

  @ReactMethod
  public void showToast(){
    Toast.makeText(reactContext, "Toast From Native", Toast.LENGTH_SHORT).show();
  }
}