
package com.mhandharbeni.benibeacon;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

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
}