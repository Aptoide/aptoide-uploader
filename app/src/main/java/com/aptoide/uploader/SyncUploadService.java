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

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    this.uploadManager = ((UploaderApplication) getApplication()).getUploadManager();
    checkUploads();
    return super.onStartCommand(intent, flags, startId);
  }

  private void checkUploads() {
    uploadManager.getUploads()
        .flatMap(__ -> uploadManager.getUploads())
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.PENDING))
        .doOnNext(upload -> uploadManager.uploadAppToRepo(upload))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }
}
