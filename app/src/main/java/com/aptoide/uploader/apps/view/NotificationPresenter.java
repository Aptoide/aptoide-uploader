package com.aptoide.uploader.apps.view;

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
    view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> uploadManager.getUploads())
        .flatMapIterable(uploads -> uploads)
        .subscribe(upload -> view.showUploadNotification(upload),throwable -> {
          throw new OnErrorNotImplementedException(throwable);});
  }
}
