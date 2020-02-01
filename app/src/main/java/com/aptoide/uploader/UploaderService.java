package com.aptoide.uploader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class UploaderService extends Service {

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY_COMPATIBILITY;
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
