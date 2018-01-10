package com.aptoide.uploader.apps;

import android.content.Context;
import android.content.Intent;
import com.aptoide.uploader.UploaderService;
import com.aptoide.uploader.upload.BackgroundService;

/**
 * Created by trinkes on 08/01/2018.
 */

public class ServiceBackgroundService implements BackgroundService {
  private final Context context;
  private final Class<UploaderService> serviceClass;

  public ServiceBackgroundService(Context context, Class<UploaderService> serviceClass) {
    this.context = context;
    this.serviceClass = serviceClass;
  }

  @Override public void enable() {
    context.startService(new Intent(context, serviceClass));
  }

  @Override public void disable() {
    context.stopService(new Intent(context, serviceClass));
  }
}
