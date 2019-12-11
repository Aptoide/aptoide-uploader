package com.aptoide.uploader.apps;

import android.annotation.SuppressLint;
import android.util.Log;
import com.aptoide.uploader.apps.network.DraftStatus;
import com.aptoide.uploader.apps.network.NoConnectivityException;
import com.aptoide.uploader.apps.network.UploaderService;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.apps.persistence.DraftPersistence;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import com.aptoide.uploader.upload.AccountProvider;
import com.aptoide.uploader.upload.BackgroundService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
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
  private final DraftPersistence draftPersistence;

  public UploadManager(UploaderService uploaderService, UploaderPersistence persistence,
      Md5Calculator md5Calculator, BackgroundService backgroundService,
      AccountProvider accountProvider, AppUploadStatusManager appUploadStatusManager,
      AppUploadStatusPersistence appUploadStatusPersistence,
      UploadProgressManager uploadProgressManager, DraftPersistence draftPersistence) {
    this.uploaderService = uploaderService;
    this.persistence = persistence;
    this.md5Calculator = md5Calculator;
    this.backgroundService = backgroundService;
    this.accountProvider = accountProvider;
    this.appUploadStatusManager = appUploadStatusManager;
    this.appUploadStatusPersistence = appUploadStatusPersistence;
    this.uploadProgressManager = uploadProgressManager;
    this.draftPersistence = draftPersistence;
  }

  public Completable upload(String storeName, String language, InstalledApp app) {
    return md5Calculator.calculate(app.getApkPath())
        .flatMapCompletable((md5 -> uploaderService.startUploadDraft(md5, language, storeName, app)
            .flatMapCompletable(draft -> draftPersistence.save(draft))));
  }

  public Observable<List<UploadDraft>> getDrafts() {
    return draftPersistence.getDrafts();
  }

  public Completable removeUploadFromPersistence(UploadDraft draft) {
    return draftPersistence.remove(draft);
  }

  public void start() {
    fillAppUploadStatusPersistence();
    handleBackgroundService();
    dispatchUploads();
    handleMetadataAdded();
    handleMetadataSet();
    handleMd5NotExistent();
    handleRetryStatus();
    handleCompletedStatus();
    handleMd5sSetStatus();
    checkAppUploadStatus();
    handleStatusSet();
    handleDraftCreatedStatus();
    handleSplitsNotExistent();
  }

  @SuppressLint("CheckResult") private void dispatchUploads() {
    accountProvider.getAccount()
        .switchMap(account -> {
          if (account.isLoggedIn()) {
            return draftPersistence.getDrafts()
                .distinctUntilChanged(
                    (previous, current) -> !draftPersistenceHasChanged(previous, current))
                .flatMapIterable(drafts -> drafts)
                .filter(draft -> draft.getStatus()
                    .equals(UploadDraft.Status.START))
                .flatMapCompletable(draft -> draftPersistence.remove(draft)
                    .andThen(uploaderService.createDraft(draft.getMd5(), draft.getInstalledApp()))
                    .flatMapCompletable(uploadDraft -> draftPersistence.save(uploadDraft)))
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

  @SuppressLint("CheckResult") private void handleDraftCreatedStatus() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.DRAFT_CREATED))
        .flatMapCompletable(draft -> draftPersistence.remove(draft)
            .andThen(uploaderService.setDraftMd5s(draft))
            .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft)))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleMd5sSetStatus() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.MD5S_SET))
        .flatMapCompletable(draft -> draftPersistence.remove(draft)
            .andThen(uploaderService.setDraftStatus(draft, DraftStatus.PENDING))
            .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft)))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleStatusSet() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.STATUS_SET))
        .flatMapCompletable(draft -> draftPersistence.remove(draft)
            .andThen(uploaderService.getDraftStatus(draft))
            .flatMapCompletable(statusSetDraft -> draftPersistence.save(statusSetDraft)))
        .subscribe();
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

  public Completable addMetadataToDraft(Metadata metadata, String md5) {
    return draftPersistence.getDrafts()
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(Upload.Status.NO_META_DATA) && draft.getMd5()
            .equals(md5))
        .flatMapCompletable(draft -> Observable.just(
            new UploadDraft(UploadDraft.Status.META_DATA_ADDED, draft.getInstalledApp(),
                draft.getMd5(), draft.getDraftId()))
            .flatMapCompletable(newDraft -> draftPersistence.remove(draft)
                .doOnComplete(() -> newDraft.setMetadata(metadata))
                .toSingleDefault(newDraft)
                .toObservable()
                .flatMapCompletable(aa -> draftPersistence.save(aa))));
  }

  @SuppressLint("CheckResult") private void handleMetadataAdded() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.META_DATA_ADDED))
        .flatMap(draft -> uploaderService.setDraftMetadata(draft))
        .flatMapCompletable(draft -> draftPersistence.save(draft))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleMetadataSet() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.METADATA_SET))
        .flatMap(draft -> {
          draft.setStatus(UploadDraft.Status.PROGRESS);
          return uploaderService.uploadFiles(draft);
        })
        .flatMapCompletable(draft -> draftPersistence.save(draft))
        .subscribe();
  }

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
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.NOT_EXISTENT))
        .flatMapCompletable(draft -> uploaderService.hasApplicationMetaData(draft)
            .flatMapCompletable(hasMetaData -> {
              if (!hasMetaData) {
                draft.setStatus(UploadDraft.Status.NO_META_DATA);
                return draftPersistence.save(draft);
              } else {
                draft.setStatus(UploadDraft.Status.PROGRESS);
                return draftPersistence.save(draft);
              }
            })
            .onErrorResumeNext(throwable -> {
              throwable.printStackTrace();
              draft.setStatus(UploadDraft.Status.CLIENT_ERROR);
              return draftPersistence.save(draft);
            }))
        .subscribe(() -> {
        }, throwable -> throwable.printStackTrace());
  }

  @SuppressLint("CheckResult") private void handleSplitsNotExistent() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.SPLITS_NOT_EXISTENT))
        .flatMap(draft -> draftPersistence.remove(draft)
            .andThen(getSplitsNotExistentPaths(draft.getSplitsToBeUploaded()))
            .flatMapObservable(list -> uploaderService.uploadSplits(draft, list)))
        .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft))
        .subscribe();
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

  @SuppressLint("CheckResult")
  private Single<List<String>> getSplitsNotExistentPaths(List<String> splitsMd5sNotExistent) {
    return Observable.fromIterable(splitsMd5sNotExistent)
        .map(md5 -> md5Calculator.getPathFromCache(md5))
        .toList();
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

  private boolean draftPersistenceHasChanged(List<UploadDraft> previousList,
      List<UploadDraft> currentList) {
    if (previousList.size() != currentList.size()) {
      return true;
    }
    for (UploadDraft previous : previousList) {
      UploadDraft current = currentList.get(previousList.indexOf(previous));
      if (!previous.getStatus()
          .equals(current.getStatus())) {
        return true;
      }
    }
    return false;
  }
}
