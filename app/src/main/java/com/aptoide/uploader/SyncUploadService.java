package com.aptoide.uploader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import java.util.List;

public class SyncUploadService extends Service {

  private UploaderPersistence persistence;
  private UploadManager uploadManager;

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    this.uploadManager = ((UploaderApplication) getApplication()).getUploadManager();
    this.persistence = ((UploaderApplication) getApplication()).getUploadPersistence();
    dispatchUploads();
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private void dispatchUploads() {
    persistence.getUploads()
        .distinctUntilChanged((previous, current) -> !hasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.PENDING))
        .flatMap(upload -> uploadManager.uploadAppToRepo(upload))
        .flatMapCompletable(upload -> persistence.save(upload))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe();
  }

  private boolean hasChanged(List<Upload> previousList, List<Upload> currentList) {
    for (Upload previous : previousList) {
      Upload current = currentList.get(currentList.indexOf(previous));
      if (!previous.getStatus()
          .equals(current.getStatus())) {
        return true;
      }
    }
    return !previousList.equals(currentList);
  }
}
