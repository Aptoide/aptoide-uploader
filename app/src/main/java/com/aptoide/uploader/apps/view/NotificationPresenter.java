package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;

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
        .flatMap(upload -> {
          if (upload.getStatus()
              .equals(Upload.Status.PROGRESS)) {
            return updateProgress(upload);
          } else {
            return Observable.just(upload);
          }
        })
        .doOnNext(upload -> showNotification(upload))
        .subscribe();
  }

  private void showNotification(Upload upload) {
    String appName = upload.getInstalledApp()
        .getName();
    String packageName = upload.getInstalledApp()
        .getPackageName();
    String md5 = upload.getMd5();

    switch (upload.getStatus()) {
      case PENDING:
        view.showPendingUploadNotification(appName, packageName);
        break;
      case CLIENT_ERROR:
        break;
      case NOT_EXISTENT:
        break;
      case NO_META_DATA:
        view.showNoMetaDataNotification(appName, packageName, md5);
        break;
      case PROGRESS:
        break;
      case COMPLETED:
        view.showCompletedUploadNotification(appName, packageName);
        break;
      case DUPLICATE:
        view.showDuplicateUploadNotification(appName, packageName);
        break;
      case FAILED:
        view.showFailedUploadNotification(appName, packageName);
        break;
      case INFECTED:
        view.showUploadInfectionNotificaton(appName, packageName);
        break;
      case PUBLISHER_ONLY:
        view.showPublisherOnlyNotification(appName, packageName);
        break;
      case INVALID_SIGNATURE:
        view.showInvalidSignatureNotification(appName, packageName);
        break;
      case META_DATA_ADDED:
        break;
      case RETRY:
        break;
      case INTELLECTUAL_RIGHTS:
        view.showIntellectualRightsNotification(appName, packageName);
        break;
      default:
        view.showErrorNotification(appName, packageName);
        break;
    }
  }

  private Observable<Upload> updateProgress(Upload upload) {
    return uploadManager.getProgress(upload.getInstalledApp()
        .getPackageName())
        .doOnNext(uploadProgress -> view.updateUploadProgress(upload.getInstalledApp()
            .getName(), upload.getInstalledApp()
            .getPackageName(), uploadProgress.getProgress()))
        .map(__ -> upload)
        .doOnError(throwable -> throwable.printStackTrace());
  }
}

