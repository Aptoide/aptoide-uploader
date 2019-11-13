package com.aptoide.uploader.apps;

import android.annotation.SuppressLint;
import android.util.Log;
import com.aptoide.uploader.apps.network.NoConnectivityException;
import com.aptoide.uploader.apps.network.UploaderService;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import com.aptoide.uploader.upload.AccountProvider;
import com.aptoide.uploader.upload.BackgroundService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class UploadManager {
  private final UploaderService uploaderService;
  private final UploaderPersistence persistence;
  private final Md5Calculator md5Calculator;
  private final BackgroundService backgroundService;
  private final AccountProvider accountProvider;
  private final AppUploadStatusManager appUploadStatusManager;
  private final AppUploadStatusPersistence appUploadStatusPersistence;
  private UploadProgressManager uploadProgressManager;

  public UploadManager(UploaderService uploaderService, UploaderPersistence persistence,
      Md5Calculator md5Calculator, BackgroundService backgroundService,
      AccountProvider accountProvider, AppUploadStatusManager appUploadStatusManager,
      AppUploadStatusPersistence appUploadStatusPersistence,
      UploadProgressManager uploadProgressManager) {
    this.uploaderService = uploaderService;
    this.persistence = persistence;
    this.md5Calculator = md5Calculator;
    this.backgroundService = backgroundService;
    this.accountProvider = accountProvider;
    this.appUploadStatusManager = appUploadStatusManager;
    this.appUploadStatusPersistence = appUploadStatusPersistence;
    this.uploadProgressManager = uploadProgressManager;
  }

  public Completable upload(String storeName, String language, InstalledApp app) {
    return md5Calculator.calculate(app.getApkPath())
        .flatMapCompletable((md5 -> uploaderService.getUpload(md5, language, storeName, app)
            .flatMapCompletable(upload -> persistence.save(upload))));
  }

  public Observable<List<Upload>> getUploads() {
    return persistence.getUploads();
  }

  public Completable removeUploadFromPersistence(Upload upload) {
    return persistence.remove(upload);
  }

  public void start() {
    fillAppUploadStatusPersistence();
    handleBackgroundService();
    dispatchUploads();
    handleMetadataAdded();
    handleMd5NotExistent();
    handleRetryStatus();
    handleCompletedStatus();
    checkAppUploadStatus();
    //handleObbMain();
    //handleObbPatch();
  }

  public Observable<UploadProgress> getProgress(String packageName) {
    return uploadProgressManager.getProgress(packageName);
  }

  @SuppressLint("CheckResult") private void fillAppUploadStatusPersistence() {
    appUploadStatusManager.getNonSystemApps()
        .toObservable()
        .flatMapIterable(apps -> apps)
        .flatMapSingle(installedApp -> md5Calculator.calculate(installedApp.getApkPath())
            .map(md5 -> new AppUploadStatus(md5, installedApp.getPackageName(), false,
                String.valueOf(installedApp.getVersionCode()))))
        .toList()
        .flatMapCompletable(installedApps -> appUploadStatusPersistence.saveAll(installedApps))
        .subscribeOn(Schedulers.computation())
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleMetadataAdded() {
    persistence.getUploads()
        .distinctUntilChanged(
            (previous, current) -> !uploadsPersistenceHasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.META_DATA_ADDED))
        .cast(MetadataUpload.class)
        .flatMap(upload -> {
          upload.setStatus(Upload.Status.PROGRESS);
          // TODO: 2019-10-18 verificar obbs aqui
          //if (upload.getInstalledApp()
          //    .getObbPatchPath() != null
          //    && upload.getInstalledApp()
          //    .getObbPatchPath() != null) {
          //}
          //if (upload.getInstalledApp()
          //    .getObbMainPath() != null) {
          //  return uploaderService.upload(upload.getMd5(), upload.getStoreName(),
          //      upload.getInstalledApp()
          //          .getName(), upload.getInstalledApp(), upload.getMetadata(),
          //      upload.getInstalledApp()
          //          .getObbMainPath());
          //}
          return uploaderService.upload(upload.getMd5(), upload.getStoreName(),
              upload.getInstalledApp()
                  .getName(), upload.getInstalledApp(), upload.getMetadata());
        })
        .flatMapCompletable(upload -> persistence.save(upload))
        .subscribe();
  }

  //@SuppressLint("CheckResult") private void handleObbMain() {
  //  persistence.getUploads()
  //      .distinctUntilChanged(
  //          (previous, current) -> !uploadsPersistenceHasChanged(previous, current))
  //      .flatMapIterable(uploads -> uploads)
  //      .filter(upload -> upload.getStatus()
  //          .equals(Upload.Status.OBB_MAIN))
  //      .flatMapCompletable(upload -> {
  //        upload.setStatus(Upload.Status.PROGRESS);
  //        return uploadApkWithMainObbToServer(upload);
  //      })
  //      .subscribe();
  //}

  //@SuppressLint("CheckResult") private void handleObbPatch() {
  //  persistence.getUploads()
  //      .distinctUntilChanged(
  //          (previous, current) -> !uploadsPersistenceHasChanged(previous, current))
  //      .flatMapIterable(uploads -> uploads)
  //      .filter(upload -> upload.getStatus()
  //          .equals(Upload.Status.OBB_PATCH))
  //      .flatMapCompletable(upload -> {
  //        upload.setStatus(Upload.Status.PROGRESS);
  //        return uploadApkWithPatchObbToServer(upload);
  //      })
  //      .subscribe();
  //}

  @SuppressLint("CheckResult") private void handleCompletedStatus() {
    persistence.getUploads()
        .distinctUntilChanged(
            (previous, current) -> !uploadsPersistenceHasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.COMPLETED))
        .flatMapCompletable(upload -> appUploadStatusPersistence.save(
            new AppUploadStatus(upload.getMd5(), upload.getInstalledApp()
                .getPackageName(), true, String.valueOf(upload.getInstalledApp()
                .getVersionCode()))))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleRetryStatus() {
    persistence.getUploads()
        .distinctUntilChanged(
            (previous, current) -> !uploadsPersistenceHasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.RETRY))
        .flatMap(upload -> appUploadStatusManager.checkUploadStatus(upload))
        .flatMapCompletable(uploadFromCheckStatus -> persistence.save(uploadFromCheckStatus))
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
          if (throwable instanceof NoConnectivityException) {
            Log.e(getClass().getSimpleName(), "NO INTERNET AVAILABLE!");
          } else {
            Log.e(getClass().getSimpleName(), throwable.getMessage());
          }
        });
  }

  @SuppressLint("CheckResult") private void handleMd5NotExistent() {
    persistence.getUploads()
        .distinctUntilChanged(
            (previous, current) -> !uploadsPersistenceHasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.NOT_EXISTENT) && !upload.isProcessed())
        .flatMapCompletable(upload -> {
          upload.setProcessed(true);
          return uploaderService.hasApplicationMetaData(upload.getInstalledApp()
              .getPackageName(), upload.getInstalledApp()
              .getVersionCode())
              .flatMapCompletable(hasMetaData -> {
                if (!hasMetaData) {
                  upload.setStatus(Upload.Status.NO_META_DATA);
                  return persistence.save(upload);
                } else {
                  /// TODO: 2019-10-18 verificar obbs aqui e enviar caso existam senao enviar so o apk
                  //if (upload.getInstalledApp()
                  //    .getObbMainPath() != null
                  //    && upload.getInstalledApp()
                  //    .getObbPatchPath() != null) {
                  //}
                  //if (upload.getInstalledApp()
                  //    .getObbMainPath() != null) {
                  //}
                  upload.setStatus(Upload.Status.PROGRESS);
                  return uploadApkToServer(upload);
                }
              })
              .onErrorResumeNext(__ -> {
                upload.setStatus(Upload.Status.CLIENT_ERROR);
                return persistence.save(upload);
              });
        })
        .subscribe(() -> {
        }, throwable -> throwable.printStackTrace());
  }

  @SuppressLint("CheckResult") private void checkAppUploadStatus() {
    accountProvider.getAccount()
        .switchMap(account -> {
          if (account.isLoggedIn() && account.hasStore()) {
            return appUploadStatusPersistence.getAppsUploadStatus()
                .distinctUntilChanged(
                    (previous, current) -> !appsPersistenceHasChanged(previous, current))
                .flatMapSingle(appUploadStatuses -> Observable.fromIterable(appUploadStatuses)
                    .map(app -> app.getMd5())
                    .toList())
                .flatMap(md5List -> appUploadStatusManager.getApks(md5List))
                .flatMap(appUploadStatuses -> Observable.fromIterable(appUploadStatuses)
                    .flatMapCompletable(
                        appUploadStatus -> appUploadStatusPersistence.save(appUploadStatus))
                    .toObservable());
          }
          return Observable.empty();
        })
        .subscribe(__ -> {
        }, throwable -> {
          if (throwable instanceof NoConnectivityException) {
            Log.e(getClass().getSimpleName(), "NO INTERNET AVAILABLE!");
          }
        });
  }

  private Completable uploadApkToServer(Upload upload) {
    return uploaderService.upload(upload.getInstalledApp(), upload.getMd5(), upload.getStoreName(),
        upload.getInstalledApp()
            .getApkPath())
        .flatMapCompletable(uploadResult -> persistence.save(uploadResult));
  }

  //private Completable uploadApkWithMainObbToServer(Upload upload) {
  //  //if metadata blah blah
  //  return uploaderService.upload(upload.getInstalledApp(), upload.getMd5(), upload.getStoreName(),
  //      upload.getInstalledApp()
  //          .getObbMainPath(), "obb_main")
  //      .flatMapCompletable(uploadResult -> persistence.save(uploadResult));
  //}

  //private Completable uploadApkWithPatchObbToServer(Upload upload) {
  //  //if metadata blah blah
  //  return uploaderService.upload(upload.getInstalledApp(), upload.getMd5(), upload.getStoreName(),
  //      upload.getInstalledApp()
  //          .getObbMainPath(), "obb_patch")
  //      .flatMapCompletable(uploadResult -> persistence.save(uploadResult));
  //}

  @SuppressLint("CheckResult") private void dispatchUploads() {
    accountProvider.getAccount()
        .switchMap(account -> {
          if (account.isLoggedIn()) {
            return persistence.getUploads()
                .distinctUntilChanged(
                    (previous, current) -> !uploadsPersistenceHasChanged(previous, current))
                .flatMapIterable(uploads -> uploads)
                .filter(upload -> upload.getStatus()
                    .equals(Upload.Status.PENDING))
                .flatMap(upload -> uploaderService.upload(upload.getMd5(), account.getStoreName(),
                    upload.getInstalledApp()
                        .getName(), upload.getInstalledApp()))
                .flatMapCompletable(upload -> persistence.save(upload))
                .toObservable();
          }
          return Observable.empty();
        })
        .subscribe(__ -> {
        }, throwable -> {
          if (throwable instanceof NoConnectivityException) {
            Log.e(getClass().getSimpleName(), "NO INTERNET AVAILABLE!");
          }
        });
  }

  private boolean uploadsPersistenceHasChanged(List<Upload> previousList,
      List<Upload> currentList) {
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

  private boolean appsPersistenceHasChanged(List<AppUploadStatus> previousList,
      List<AppUploadStatus> currentList) {
    if (previousList.size() != currentList.size()) {
      return true;
    }
    return false;
  }
}
