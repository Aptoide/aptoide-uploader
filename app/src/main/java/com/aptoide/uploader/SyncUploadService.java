package com.aptoide.uploader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.UploadManager;
import io.reactivex.exceptions.OnErrorNotImplementedException;

/**
 * Created by filipe on 27-12-2017.
 */

public class SyncUploadService extends Service {

  private UploadManager uploadManager;

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    this.uploadManager = ((UploaderApplication) getApplication()).getUploadManager();
    dispatchUploads();
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private void dispatchUploads() {
    uploadManager.getUploads()
        .flatMap(__ -> uploadManager.getUploads())
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.PENDING))
        .flatMap(upload -> uploadManager.uploadAppToRepo(upload))
        .subscribe(__ -> stopSelf(), throwable -> {
          stopSelf();
          throw new OnErrorNotImplementedException(throwable);
        });
  }
}
