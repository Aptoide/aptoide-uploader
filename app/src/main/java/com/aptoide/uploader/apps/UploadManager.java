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
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class UploadManager {
  private static final int UPLOADS_IN_PARALLEL = 2;
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

  private Single<List<UploadDraft>> applyQueue(List<UploadDraft> drafts) {
    if (drafts.size() > 1) {
      return Single.just(drafts.subList(0, UPLOADS_IN_PARALLEL));
    } else {
      ArrayList<UploadDraft> uploadDrafts = new ArrayList<>();
      uploadDrafts.add(drafts.get(0));
      return Single.just(uploadDrafts);
    }
  }

  public void start() {
    fillAppUploadStatusPersistence();
    handleBackgroundService();
    dispatchUploads();
    handleMetadataAdded();
    handleMetadataSet();
    handleCompletedStatus();
    handleStatusSetDraft();
    checkAppUploadStatus();
    handleSplitsNotExistent();
  }

  public Completable upload(String storeName, String language, InstalledApp app) {
    return md5Calculator.calculate(app.getApkPath())
        .flatMapCompletable((md5 -> uploaderService.startUploadDraft(md5, language, storeName, app)
            .flatMapCompletable(draft -> draftPersistence.save(draft))));
  }

  public Observable<List<UploadDraft>> getDrafts() {
    return draftPersistence.getDrafts();
  }

  public Completable removeUploadFromPersistence(String md5) {
    return draftPersistence.remove(md5);
  }

  private Observable<List<UploadDraft>> getDraftsInProgress() {
    return draftPersistence.getDrafts()
        .flatMap(drafts -> Observable.fromIterable(drafts)
            .filter(draft -> draft.isInProgress())
            .toList()
            .toObservable())
        .throttleLast(750, TimeUnit.MILLISECONDS);
  }

  public Single<List<UploadDraft>> getDraftsInQueue() {
    return draftPersistence.getDrafts()
        .firstOrError()
        .doOnSuccess(drafts -> Log.d("nz-xt", "getDrafts: " + drafts.toString()))
        .flatMap(drafts -> Observable.fromIterable(drafts)
            .filter(draft -> draft.getStatus()
                .equals(UploadDraft.Status.IN_QUEUE))
            .toList());
  }

  @SuppressLint("CheckResult") private void dispatchUploads() {
    accountProvider.getAccount()
        .switchMap(account -> {
          if (account.isLoggedIn()) {
            return getDraftsInProgress().filter(
                progressDrafts -> progressDrafts.size() < UPLOADS_IN_PARALLEL)
                .doOnNext(drafts -> Log.d("nzxt", "drafts in progress: " + drafts.size()))
                .throttleLast(250, TimeUnit.MILLISECONDS)
                .flatMapSingle(__ -> getDraftsInQueue().doOnSuccess(
                    drafts -> Log.d("nzxt", "drafts in queue: " + drafts.size())))
                .filter(uploadDrafts -> !uploadDrafts.isEmpty())
                .flatMapSingle(this::applyQueue)
                .flatMapIterable(uploadDraft -> uploadDraft)
                .flatMapCompletable(this::upload)
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

  private Completable upload(UploadDraft draft) {
    return createDraft(draft).flatMap(this::setMd5)
        .flatMap(this::setPending)
        .flatMap(this::getDraftStatus)
        .filter(md5NotExistent())
        .flatMapCompletable(
            statusSetDraft -> hasApplicationMetadata(statusSetDraft).flatMap(this::setDraft)
                .flatMap(this::uploadFiles)
                .filter(waitingUploadConfirmation())
                .flatMapCompletable(
                    uploadedDraft -> setPending(uploadedDraft).flatMap(this::getDraftStatus)
                        .toCompletable()));
  }

  @NotNull private Predicate<UploadDraft> md5NotExistent() {
    return statusDraft -> statusDraft.getStatus()
        .equals(UploadDraft.Status.NOT_EXISTENT);
  }

  @NotNull private Predicate<UploadDraft> waitingUploadConfirmation() {
    return uploadedDraft -> uploadedDraft.getStatus()
        .equals(UploadDraft.Status.WAITING_UPLOAD_CONFIRMATION);
  }

  private Single<UploadDraft> uploadFiles(UploadDraft draftDraft) {
    return uploaderService.uploadFiles(draftDraft)
        .singleOrError()
        .flatMap(uploadedDraft -> draftPersistence.save(
            new UploadDraft(UploadDraft.Status.PROGRESS, uploadedDraft.getInstalledApp(),
                uploadedDraft.getMd5(), uploadedDraft.getDraftId(), uploadedDraft.getMetadata()))
            .toSingleDefault(uploadedDraft));
  }

  private Single<UploadDraft> setDraft(UploadDraft metaDataDraft) {
    return uploaderService.setDraftStatus(metaDataDraft, DraftStatus.DRAFT)
        .singleOrError()
        .flatMap(draftDraft -> draftPersistence.save(
            new UploadDraft(UploadDraft.Status.PROGRESS, draftDraft.getInstalledApp(),
                draftDraft.getMd5(), draftDraft.getDraftId(), draftDraft.getMetadata()))
            .toSingleDefault(draftDraft));
  }

  private Single<UploadDraft> hasApplicationMetadata(UploadDraft draft) {
    return uploaderService.hasApplicationMetaData(draft)
        .singleOrError()
        .flatMap(statusDraft -> draftPersistence.save(statusDraft)
            .toSingleDefault(statusDraft));
  }

  private Single<UploadDraft> getDraftStatus(UploadDraft draft) {
    return uploaderService.getDraftStatus(draft)
        .singleOrError()
        .flatMap(statusDraft -> draftPersistence.save(statusDraft)
            .toSingleDefault(statusDraft));
  }

  private Single<UploadDraft> setPending(UploadDraft draft) {
    return uploaderService.setDraftStatus(draft, DraftStatus.PENDING)
        .singleOrError()
        .flatMap(pendingDraft -> draftPersistence.save(pendingDraft)
            .toSingleDefault(pendingDraft));
  }

  private Single<UploadDraft> setMd5(UploadDraft draft) {
    return uploaderService.setDraftMd5s(draft)
        .singleOrError()
        .flatMap(md5draft -> draftPersistence.save(md5draft)
            .toSingleDefault(md5draft));
  }

  private Single<UploadDraft> createDraft(UploadDraft draft) {
    return uploaderService.createDraft(draft.getMd5(), draft.getInstalledApp())
        .singleOrError()
        .flatMap(uploadDraft -> draftPersistence.save(uploadDraft)
            .toSingleDefault(uploadDraft));
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
    return previousList.size() != currentList.size();
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
