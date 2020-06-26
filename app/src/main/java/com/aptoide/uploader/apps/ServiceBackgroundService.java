package com.aptoide.uploader.apps;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.aptoide.uploader.NotificationService;
import com.aptoide.uploader.upload.BackgroundService;

/**
 * Created by trinkes on 08/01/2018.
 */

public class ServiceBackgroundService implements BackgroundService {
  private final Context context;
  private final Class<NotificationService> serviceClass;

  public ServiceBackgroundService(Context context, Class<NotificationService> serviceClass) {
    this.context = context;
    this.serviceClass = serviceClass;
  }

  @Override public void enable() {
    context.startService(new Intent(context, serviceClass));
  }

  @Override public void disable() {
    Log.d("uploadService", "Going to disable the service");
    context.stopService(new Intent(context, serviceClass));
  }
}
