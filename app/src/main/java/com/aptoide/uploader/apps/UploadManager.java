package com.aptoide.uploader.apps;

import android.annotation.SuppressLint;
import android.util.Log;
import com.aptoide.uploader.apps.network.DraftStatus;
import com.aptoide.uploader.apps.network.NoConnectivityException;
import com.aptoide.uploader.apps.network.UploaderService;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.apps.persistence.DraftPersistence;
import com.aptoide.uploader.upload.AccountProvider;
import com.aptoide.uploader.upload.BackgroundService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UploadManager {
  private final UploaderService uploaderService;
  private final Md5Calculator md5Calculator;
  private final BackgroundService backgroundService;
  private final AccountProvider accountProvider;
  private final AppUploadStatusManager appUploadStatusManager;
  private final AppUploadStatusPersistence appUploadStatusPersistence;
  private final DraftPersistence draftPersistence;
  private UploadProgressManager uploadProgressManager;

  public UploadManager(UploaderService uploaderService, Md5Calculator md5Calculator,
      BackgroundService backgroundService, AccountProvider accountProvider,
      AppUploadStatusManager appUploadStatusManager,
      AppUploadStatusPersistence appUploadStatusPersistence,
      UploadProgressManager uploadProgressManager, DraftPersistence draftPersistence) {
    this.uploaderService = uploaderService;
    this.md5Calculator = md5Calculator;
    this.backgroundService = backgroundService;
    this.accountProvider = accountProvider;
    this.appUploadStatusManager = appUploadStatusManager;
    this.appUploadStatusPersistence = appUploadStatusPersistence;
    this.uploadProgressManager = uploadProgressManager;
    this.draftPersistence = draftPersistence;
  }

  public void start() {
    fillAppUploadStatusPersistence();
    handleBackgroundService();
    dispatchUploads();
    handleMetadataAdded();
    handleMetadataSet();
    handleMd5NotExistent();
    handleCompletedStatus();
    handleMd5sSet();
    handleStatusSetDraft();
    checkAppUploadStatus();
    handleStatusSetMd5();
    handleProgressStatus();
    handleAwaitingUploadConfirmation();
    handleDraftCreatedStatus();
    handleSplitsNotExistent();
    handleSetStatusToDraft();
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

  public Observable<Boolean> hasDraftsInProgress() {
    return draftPersistence.getDrafts()
        .flatMap(drafts -> Observable.fromIterable(drafts)
            .filter(draft -> draft.getStatus() != UploadDraft.Status.START)
            .toList()
            .toObservable())
        .throttleLast(750, TimeUnit.MILLISECONDS)
        .map(list -> !list.isEmpty());
  }

  public Observable<List<UploadDraft>> getDraftsInStart() {
    return draftPersistence.getDrafts()
        .flatMapSingle(drafts -> Observable.fromIterable(drafts)
            .filter(draft -> draft.getStatus()
                .equals(UploadDraft.Status.START))
            .toList());
  }

  @SuppressLint("CheckResult") private void dispatchUploads() {
    accountProvider.getAccount()
        .switchMap(account -> {
          if (account.isLoggedIn()) {
            return hasDraftsInProgress().filter(hasDrafts -> !hasDrafts)
                .flatMapSingle(__ -> getDraftsInStart().firstOrError())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .flatMapCompletable(
                    draft -> uploaderService.createDraft(draft.getMd5(), draft.getInstalledApp())
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
          throwable.printStackTrace();
        });
  }

  @SuppressLint("CheckResult") private void handleDraftCreatedStatus() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.DRAFT_CREATED))
        .flatMapCompletable(draft -> uploaderService.setDraftMd5s(draft)
            .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft)))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleMd5sSet() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.MD5S_SET))
        .flatMapCompletable(draft -> uploaderService.setDraftStatus(draft, DraftStatus.PENDING)
            .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft)))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleStatusSetMd5() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.STATUS_SET_PENDING))
        .flatMapCompletable(draft -> uploaderService.getDraftStatus(draft)
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

  public Completable handleNoMetadata(Metadata metadata, String md5) {
    return draftPersistence.getDrafts()
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.NO_META_DATA) && draft.getMd5()
            .equals(md5))
        .flatMapCompletable(draft -> Observable.just(
            new UploadDraft(UploadDraft.Status.META_DATA_ADDED, draft.getInstalledApp(),
                draft.getMd5(), draft.getDraftId()))
            .doOnNext(newDraft -> newDraft.setMetadata(metadata))
            .flatMapCompletable(aa -> draftPersistence.save(aa)));
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
        .flatMapCompletable(draft -> {
          UploadDraft uploadDraft =
              new UploadDraft(UploadDraft.Status.SET_STATUS_TO_DRAFT, draft.getInstalledApp(),
                  draft.getMd5(), draft.getDraftId(), draft.getMetadata());
          return draftPersistence.save(uploadDraft);
        })
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleSetStatusToDraft() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.SET_STATUS_TO_DRAFT))
        .flatMap(draft -> uploaderService.setDraftStatus(draft, DraftStatus.DRAFT))
        .flatMapCompletable(draft -> {
          draft.setStatus(UploadDraft.Status.PROGRESS);
          return draftPersistence.save(draft);
        })
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleProgressStatus() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.PROGRESS))
        .flatMap(draft -> uploaderService.uploadFiles(draft))
        .flatMapCompletable(draft -> draftPersistence.save(draft))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleCompletedStatus() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.COMPLETED))
        .flatMapCompletable(upload -> appUploadStatusPersistence.save(
            new AppUploadStatus(upload.getMd5(), upload.getInstalledApp()
                .getPackageName(), true, String.valueOf(upload.getInstalledApp()
                .getVersionCode()))))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleBackgroundService() {
    draftPersistence.getDrafts()
        .flatMapSingle(drafts -> Observable.fromIterable(drafts)
            .filter(draft -> draft.getStatus()
                .equals(UploadDraft.Status.COMPLETED))
            .toList())
        .map(drafts -> drafts.size() > 0)
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
              if (hasMetaData) {
                UploadDraft uploadDraft =
                    new UploadDraft(UploadDraft.Status.SET_STATUS_TO_DRAFT, draft.getInstalledApp(),
                        draft.getMd5(), draft.getDraftId());
                return draftPersistence.save(uploadDraft);
              } else {
                UploadDraft uploadDraft =
                    new UploadDraft(UploadDraft.Status.NO_META_DATA, draft.getInstalledApp(),
                        draft.getMd5(), draft.getDraftId());
                return draftPersistence.save(uploadDraft);
              }
            })
            .onErrorResumeNext(throwable -> {
              throwable.printStackTrace();
              UploadDraft uploadDraft =
                  new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
                      draft.getMd5(), draft.getDraftId());
              return draftPersistence.save(uploadDraft);
            }))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleSplitsNotExistent() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.UPLOAD_PENDING))
        .flatMapCompletable(draft -> uploaderService.setDraftStatus(draft, DraftStatus.DRAFT)
            .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft)))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleStatusSetDraft() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.STATUS_SET_DRAFT))
        .flatMap(draft -> getSplitsNotExistentPaths(draft.getSplitsToBeUploaded()).toObservable()
            .flatMap(list -> uploaderService.uploadSplits(draft, list)))
        .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft))
        .subscribe();
  }

  @SuppressLint("CheckResult") private void handleAwaitingUploadConfirmation() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.WAITING_UPLOAD_CONFIRMATION))
        .flatMapCompletable(draft -> uploaderService.setDraftStatus(draft, DraftStatus.PENDING)
            .flatMapCompletable(pendingDraft -> draftPersistence.save(pendingDraft)))
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
    if (splitsMd5sNotExistent != null) {
      return Observable.fromIterable(splitsMd5sNotExistent)
          .map(md5 -> md5Calculator.getPathFromCache(md5))
          .toList();
    }
    return Single.error(new IllegalStateException("md5 cannot be null"));
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
