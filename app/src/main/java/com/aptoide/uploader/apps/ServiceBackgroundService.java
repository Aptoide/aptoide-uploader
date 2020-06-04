package com.aptoide.uploader.apps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.aptoide.uploader.NotificationApplicationView;
import com.aptoide.uploader.upload.BackgroundService;

/**
 * Created by trinkes on 08/01/2018.
 */

public class ServiceBackgroundService implements BackgroundService {
  private final Context context;
  private final Class<NotificationApplicationView> serviceClass;

  public ServiceBackgroundService(Context context,
      Class<NotificationApplicationView> serviceClass) {
    this.context = context;
    this.serviceClass = serviceClass;
  }

  @Override public int enable() {
    context.startService(new Intent(context, serviceClass));
    return Service.START_STICKY;
  }

  @Override public void disable() {
    Log.i("LOL", "Going to desable the service");
    context.stopService(new Intent(context, serviceClass));
  }
}
