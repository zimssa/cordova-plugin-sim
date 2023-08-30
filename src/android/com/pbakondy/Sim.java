// MCC and MNC codes on Wikipedia
// http://en.wikipedia.org/wiki/Mobile_country_code

// Mobile Network Codes (MNC) for the international identification plan for public networks and subscriptions
// http://www.itu.int/pub/T-SP-E.212B-2014

// class TelephonyManager
// http://developer.android.com/reference/android/telephony/TelephonyManager.html
// https://github.com/android/platform_frameworks_base/blob/master/telephony/java/android/telephony/TelephonyManager.java

// permissions
// http://developer.android.com/training/permissions/requesting.html

// Multiple SIM Card Support
// https://developer.android.com/about/versions/android-5.1.html

// class SubscriptionManager
// https://developer.android.com/reference/android/telephony/SubscriptionManager.html
// https://github.com/android/platform_frameworks_base/blob/master/telephony/java/android/telephony/SubscriptionManager.java

// class SubscriptionInfo
// https://developer.android.com/reference/android/telephony/SubscriptionInfo.html
// https://github.com/android/platform_frameworks_base/blob/master/telephony/java/android/telephony/SubscriptionInfo.java

// Cordova Permissions API
// https://cordova.apache.org/docs/en/latest/guide/platforms/android/plugin.html#android-permissions

package com.pbakondy;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class Sim extends CordovaPlugin {
  private static final String LOG_TAG = "CordovaPluginSim";


  private static final String GET_SIM_INFO = "getSimInfo";
  private static final String HAS_READ_PERMISSION = "hasReadPermission";
  private static final String REQUEST_READ_PERMISSION = "requestReadPermission";

  private CallbackContext callback;

  @SuppressLint("HardwareIds")
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    callback = callbackContext;

    if (GET_SIM_INFO.equals(action)) {
      Context context = this.cordova.getActivity().getApplicationContext();

      TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

      //mkkim : 전화번호만 받도록 변경, 코드 내에서도 다른 것 안씀
      String phoneNumber = "";
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED ||
              ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED ||
              ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        phoneNumber = manager.getLine1Number();
      }

      JSONObject result = new JSONObject();
      result.put("phoneNumber", phoneNumber);
      callbackContext.success(result);

      return true;
    } else if (HAS_READ_PERMISSION.equals(action)) {
      hasReadPermission();
      return true;
    } else if (REQUEST_READ_PERMISSION.equals(action)) {
      requestReadPermission();
      return true;
    } else {
      return false;
    }
  }

  private String getPhoneNumberPermission() {
    if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
      return Manifest.permission.READ_PHONE_STATE;
    } else {
      return Manifest.permission.READ_PHONE_NUMBERS;
    }

  }

  private void hasReadPermission() {
    String permission = getPhoneNumberPermission();
    this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
      simPermissionGranted(permission)));
  }

  private void requestReadPermission() {
    String permission = getPhoneNumberPermission();
    requestPermission(permission);
  }

  private boolean simPermissionGranted(String type) {
    if (Build.VERSION.SDK_INT < 23) {
      return true;
    }
    return cordova.hasPermission(type);
  }

  private void requestPermission(String type) {
    LOG.i(LOG_TAG, "requestPermission");
    if (!simPermissionGranted(type)) {
      cordova.requestPermission(this, 12345, type);
    } else {
      this.callback.success();
    }
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException
  {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      this.callback.success();
    } else {
      this.callback.error("Permission denied");
    }
  }
}