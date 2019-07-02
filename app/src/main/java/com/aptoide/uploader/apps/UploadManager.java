package com.aptoide.uploader.apps;

import android.annotation.SuppressLint;
import com.aptoide.uploader.apps.network.UploaderService;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import com.aptoide.uploader.upload.AccountProvider;
import com.aptoide.uploader.upload.BackgroundService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import java.util.List;

public class UploadManager {
  private final UploaderService uploaderService;
  private final UploaderPersistence persistence;
  private final Md5Calculator md5Calculator;
  private final BackgroundService backgroundService;
  private final AccountProvider accountProvider;

  public UploadManager(UploaderService uploaderService, UploaderPersistence persistence,
      Md5Calculator md5Calculator, BackgroundService backgroundService,
      AccountProvider accountProvider) {
    this.uploaderService = uploaderService;
    this.persistence = persistence;
    this.md5Calculator = md5Calculator;
    this.backgroundService = backgroundService;
    this.accountProvider = accountProvider;
  }

  public Completable upload(String storeName, String language, InstalledApp app) {
    return md5Calculator.calculate(app)
        .flatMapCompletable((md5 -> uploaderService.getUpload(md5, language, storeName, app)
            .flatMapCompletable(upload -> {
              if (upload.isUploaded()) {
                if (!upload.hasProposedData()) {
                  return persistence.save(upload);
                }
              }
              return persistence.save(upload);
            })));
  }

  public Observable<List<Upload>> getUploads() {
    return persistence.getUploads();
  }

  public void start() {
    handleBackgroundService();
    dispatchUploads();
    handleMetadataAdded();
    handleMd5NotExistent();
  }

  @SuppressLint("CheckResult") private void handleMetadataAdded() {
    persistence.getUploads()
        .distinctUntilChanged((previous, current) -> !hasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.META_DATA_ADDED))
        .cast(MetadataUpload.class)
        .flatMapCompletable(upload -> uploaderService.upload(upload.getInstalledApp()
            .getApkPath(), upload.getMetadata()))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleBackgroundService() {
    persistence.getUploads()
        .flatMapSingle(uploads -> Observable.fromIterable(uploads)
            .filter(upload -> upload.getStatus()
                .equals(Upload.Status.COMPLETED))
            .toList())
        .map(uploads -> uploads.size() > 0)
        .distinctUntilChanged()
        .subscribe(hasUploadsRunning -> {
          if (hasUploadsRunning) {
            backgroundService.enable();
          } else {
            backgroundService.disable();
          }
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  @SuppressLint("CheckResult") private void handleMd5NotExistent() {
    persistence.getUploads()
        .distinctUntilChanged((previous, current) -> !hasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.NOT_EXISTENT))
        .flatMapCompletable(upload -> uploaderService.hasApplicationMetaData(
            upload.getInstalledApp()
                .getPackageName(), upload.getInstalledApp()
                .getVersionCode())
            .flatMapCompletable(hasMetaData -> {
              if (!hasMetaData) {
                upload.setStatus(Upload.Status.NO_META_DATA);
                return persistence.save(upload);
              } else {
                return uploadApkToServer(upload);
              }
            }))
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private Completable uploadApkToServer(Upload upload) {
    return uploaderService.upload(upload.getInstalledApp()
        .getApkPath());
  }

  @SuppressLint("CheckResult") private void dispatchUploads() {
    accountProvider.getAccount()
        .switchMap(account -> {
          if (account.isLoggedIn()) {
            return persistence.getUploads()
                .distinctUntilChanged((previous, current) -> !hasChanged(previous, current))
                .flatMapIterable(uploads -> uploads)
                .filter(upload -> upload.getStatus()
                    .equals(Upload.Status.PENDING))
                .flatMap(upload -> uploaderService.upload(upload.getMd5(), account.getStoreName(),
                    upload.getInstalledApp()
                        .getName(), upload.hasProposedData(), upload.getInstalledApp()))
                .flatMapCompletable(upload -> persistence.save(upload))
                .toObservable();
          }
          return Observable.empty();
        })
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private boolean hasChanged(List<Upload> previousList, List<Upload> currentList) {
    if (previousList.size() != currentList.size()) {
      return true;
    }
    for (Upload previous : previousList) {
      Upload current = currentList.get(previousList.indexOf(previous));
      if (!previous.getStatus()
          .equals(current.getStatus())) {
        return true;
      }
    }
    return !previousList.equals(currentList);
  }
}
