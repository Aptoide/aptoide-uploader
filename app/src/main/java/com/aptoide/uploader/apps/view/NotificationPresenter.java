package com.aptoide.uploader.apps.view;

import android.util.Log;
import com.aptoide.uploader.apps.UploadDraft;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.notifications.UploadNotification;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;

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
        .flatMap(__ -> uploadManager.getDrafts())
        .throttleLast(250, TimeUnit.MILLISECONDS)
        .flatMapIterable(drafts -> drafts)
        .filter(uploadDraft -> !uploadDraft.getStatus()
            .equals(UploadDraft.Status.IN_QUEUE))
        .flatMap(draft -> {
          if (draft.getStatus()
              .equals(UploadDraft.Status.PROGRESS)) {
            return updateProgress(draft);
          } else {
            return Observable.just(draft);
          }
        })
        .map(draft -> mapToNotification(draft.getInstalledApp()
            .getName(), draft.getInstalledApp()
            .getPackageName(), draft.getStatus(), draft.getMd5()))
        .doOnNext(d -> Log.d("notiticationz1", "going to show notification " + d.toString()))
        .distinctUntilChanged(notification -> notification.getType())
        .doOnNext(d -> Log.d("notificationz2", "going to show notification 2" + d.toString()))
        .flatMapCompletable(notification -> showNotification(notification))
        .subscribe();
  }

  private UploadNotification mapToNotification(String appName, String packageName,
      UploadDraft.Status status, String md5) {
    switch (status) {
      case METADATA_SET:
      case DRAFT_CREATED:
      case MD5S_SET:
      case STATUS_SET_DRAFT:
      case STATUS_SET_PENDING:
      case WAITING_UPLOAD_CONFIRMATION:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.INDETERMINATE);
      case UPLOAD_PENDING:
      case META_DATA_ADDED:
      case SET_STATUS_TO_DRAFT:
      case NOT_EXISTENT:
      case PROGRESS:
      case IN_QUEUE:
        return new UploadNotification(appName, packageName, md5, UploadNotification.Type.HIDDEN);
      case NO_META_DATA:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.MORE_INFO_NEEDED);
      case COMPLETED:
        return new UploadNotification(appName, packageName, md5, UploadNotification.Type.COMPLETED);
      case DUPLICATE:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.ALREADY_IN_STORE);
      case EXCEEDED_GET_RETRIES:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.CLIENT_TIMEOUT);
      case INFECTED:
        return new UploadNotification(appName, packageName, md5, UploadNotification.Type.INFECTED);
      case PUBLISHER_ONLY:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.PUBLISHER_ONLY);
      case INVALID_SIGNATURE:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.INVALID_SIGNATURE);
      case INTELLECTUAL_RIGHTS:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.CANNOT_DISTRIBUTE);
      case CATAPPULT_CERTIFIED:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.CATAPPULT_CERTIFIED);
      case APP_BUNDLE:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.APP_BUNDLE_NOT_SUPPORTED);
      case ANTI_SPAM_RULE:
        return new UploadNotification(appName, packageName, md5, UploadNotification.Type.ANTI_SPAM);
      case UPLOAD_FAILED_RETRY:
        return new UploadNotification(appName, packageName, md5, UploadNotification.Type.TRY_AGAIN);
      case UPLOAD_FAILED:
        return new UploadNotification(appName, packageName, md5, UploadNotification.Type.FAILED);
      case UNKNOWN_ERROR_RETRY:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.ERROR_TRY_AGAIN);
      case CLIENT_ERROR:
      case UNKNOWN_ERROR:
      default:
        return new UploadNotification(appName, packageName, md5,
            UploadNotification.Type.UNKNOWN_ERROR);
    }
  }

  private Completable showNotification(UploadNotification notification) {
    Log.d("notificationz3", "going to show notification 3"
        + notification.getType()
        + " installedapp= "
        + notification.getPackageName());
    String appName = notification.getAppName();
    String packageName = notification.getPackageName();
    String md5 = notification.getMd5();

    switch (notification.getType()) {
      case INDETERMINATE:
        view.showPendingUploadNotification(appName, packageName);
        break;
      case HIDDEN:
        break;
      case MORE_INFO_NEEDED:
        view.showNoMetaDataNotification(appName, packageName, md5);
        break;
      case COMPLETED:
        view.showCompletedUploadNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case ALREADY_IN_STORE:
        view.showDuplicateUploadNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case CLIENT_TIMEOUT:
        view.showGetRetriesExceededNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case INFECTED:
        view.showUploadInfectionNotificaton(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case PUBLISHER_ONLY:
        view.showPublisherOnlyNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case INVALID_SIGNATURE:
        view.showInvalidSignatureNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case CANNOT_DISTRIBUTE:
        view.showIntellectualRightsNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case CATAPPULT_CERTIFIED:
        view.showCatappultCertifiedNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case APP_BUNDLE_NOT_SUPPORTED:
        view.showAppBundleNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case ANTI_SPAM:
        view.showAntiSpamRuleNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case TRY_AGAIN:
        view.showFailedUploadWithRetryNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case FAILED:
        view.showFailedUploadNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case ERROR_TRY_AGAIN:
        view.showUnknownErrorRetryNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case UNKNOWN_ERROR:
      default:
        view.showUnknownErrorNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
    }
    return Completable.complete();
  }

  private Observable<UploadDraft> updateProgress(UploadDraft draft) {
    Log.d("nzxt", "updateProgress called: " + draft.getInstalledApp()
        .getName());
    return uploadManager.getProgress(draft.getInstalledApp()
        .getPackageName())
        .cache()
        .sample(1000, TimeUnit.MILLISECONDS)
        .doOnNext(uploadProgress -> view.updateUploadProgress(draft.getInstalledApp()
            .getName(), draft.getInstalledApp()
            .getPackageName(), uploadProgress.getProgress()))
        .map(__ -> draft)
        .doOnError(__ -> view.showUnknownErrorRetryNotification(draft.getInstalledApp()
            .getName(), draft.getInstalledApp()
            .getPackageName()));
  }
}

