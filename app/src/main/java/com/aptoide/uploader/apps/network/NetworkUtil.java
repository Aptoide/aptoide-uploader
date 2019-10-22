package com.aptoide.uploader.apps.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

  public static boolean isOnline(Context context) {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
    return (netInfo != null && netInfo.isConnected());
  }
}
