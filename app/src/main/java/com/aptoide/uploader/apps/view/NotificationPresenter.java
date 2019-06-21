package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class NotificationPresenter implements Presenter {
  private final NotificationView view;
  private final UploadManager uploadManager;

  public NotificationPresenter(NotificationView view, UploadManager uploadManager) {
    this.view = view;
    this.uploadManager = uploadManager;
  }

  @Override public void present() {
    checkUploads();
  }

  private void checkUploads() {
    view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> uploadManager.getUploads())
        .flatMapIterable(uploads -> uploads)
        .subscribe(upload -> showNotification(upload), throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private void showNotification(Upload upload) {
    String appName = upload.getInstalledApp().getName();
    String packageName = upload.getInstalledApp().getPackageName();
    String md5 = upload.getMd5();

    switch (upload.getStatus()) {
      case PENDING:
        view.showPendingUploadNotification();
        break;
      case NO_META_DATA:
        view.showNoMetaDataNotification(appName,packageName, md5);
        break;
      case PROGRESS:
        view.showProgressUploadNotification();
        break;
      case COMPLETED:
        view.showCompletedUploadNotification();
        break;
      case DUPLICATE:
        view.showDuplicateUploadNotification(appName, packageName);
        break;
    }
  }
}
