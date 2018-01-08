package com.aptoide.uploader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SyncUploadService extends Service {

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY_COMPATIBILITY;
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
