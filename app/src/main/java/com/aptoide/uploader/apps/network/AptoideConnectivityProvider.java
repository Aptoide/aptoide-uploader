package com.aptoide.uploader.apps.network;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AptoideConnectivityProvider implements ConnectivityProvider {

  private final ConnectivityManager connectivityManager;

  public AptoideConnectivityProvider(ConnectivityManager connectivityManager) {
    this.connectivityManager = connectivityManager;
  }

  @Override public Boolean hasConnectivity() {
    NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
    return (netInfo != null && netInfo.isConnected());
  }

  @Override public Boolean isOnWifiNetwork() {
    NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
    return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
  }
}
