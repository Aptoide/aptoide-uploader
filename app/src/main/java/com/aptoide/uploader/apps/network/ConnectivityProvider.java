package com.aptoide.uploader.apps.network;

public interface ConnectivityProvider {
  Boolean hasConnectivity();

  Boolean isOnWifiNetwork();
}
