package com.aptoide.uploader.apps.network;

import android.content.Context;

public class AptoideConnectivityProvider implements ConnectivityProvider {

  private final Context context;

  public AptoideConnectivityProvider(Context context) {
    this.context = context;
  }

  @Override public Boolean hasConnectivity() {
    return NetworkUtil.isOnline(context);
  }
}
