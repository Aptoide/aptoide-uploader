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
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
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
  private final QueueManager queueManager = new QueueManager(UPLOADS_IN_PARALLEL);
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
    handleCompletedStatus();
    checkAppUploadStatus();
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

  public void removeUploadFromQueue(String md5) {
    getDraftsInProgress().flatMapIterable(drafts -> drafts)
        .filter(draft -> draft.getMd5()
            .equals(md5))
        .doOnNext(queueManager::remove)
        .subscribe();
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
                .doOnNext(drafts -> Log.d("upload", "drafts in progress: " + drafts.size()))
                .throttleLast(250, TimeUnit.MILLISECONDS)
                .flatMapSingle(__ -> getDraftsInQueue().doOnSuccess(
                    drafts -> Log.d("upload", "drafts in queue: " + drafts.size())))
                .filter(uploadDrafts -> !uploadDrafts.isEmpty())
                .map(queueManager::applyQueue)
                .flatMap(uploadDraftList -> Observable.fromIterable(uploadDraftList)
                    .flatMapCompletable(uploadDraft -> upload(uploadDraft).doOnTerminate(
                        () -> queueManager.remove(uploadDraft)))
                    .toObservable());
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
    return createDraft(draft).doOnSuccess(
        uploadDraft -> Log.d("upload", "draft created" + uploadDraft.toString()))
        .flatMap(this::setMd5)
        .doOnSuccess(uploadDraft -> Log.d("upload", "setted md5 " + uploadDraft.toString()))
        .flatMap(this::setPending)
        .doOnSuccess(uploadDraft -> Log.d("upload", "setted PENDING " + uploadDraft.toString()))
        .flatMap(this::getDraftStatus)
        .doOnSuccess(uploadDraft -> Log.d("upload", "got status " + uploadDraft.toString()))
        .flatMapCompletable(uploadDraft -> {
          if (uploadDraft.getStatus()
              .equals(UploadDraft.Status.NOT_EXISTENT)) {
            return hasApplicationMetadata(uploadDraft).doOnSuccess(uploadDraft1 -> Log.d("upload",
                "hasApplicationMetadata " + uploadDraft1.toString()))
                .flatMap(handleHasMetadataResult())
                .doOnSuccess(uploadDraft1 -> Log.d("upload",
                    "handle metadata result " + uploadDraft1.toString()))
                .flatMap(metaDataDraft -> setDraft(metaDataDraft, UploadDraft.Status.PROGRESS))
                .doOnSuccess(
                    uploadDraft1 -> Log.d("upload", "setted DRAFT" + uploadDraft1.toString()))
                .flatMap(this::uploadFiles)
                .doOnSuccess(
                    uploadDraft1 -> Log.d("upload", "files uploaded" + uploadDraft1.toString()))
                .filter(waitingUploadConfirmation())
                .doOnSuccess(
                    uploadDraft1 -> Log.d("upload", "upload complete " + uploadDraft1.toString()))
                .flatMapCompletable(uploadedDraft -> setPending(uploadedDraft).doOnSuccess(
                    uploadDraft1 -> Log.d("upload", "setted PENDING " + uploadDraft1.toString()))
                    .flatMap(this::getDraftStatus)
                    .doOnSuccess(
                        uploadDraft1 -> Log.d("upload", "got status " + uploadDraft1.toString()))
                    .toCompletable());
          } else if (uploadDraft.getStatus()
              .equals(UploadDraft.Status.UPLOAD_PENDING)) {
            return setDraft(uploadDraft, UploadDraft.Status.STATUS_SET_DRAFT).doOnSuccess(
                uploadDraft1 -> Log.d("upload",
                    "setted STATUS_SET_DRAFT " + uploadDraft1.toString()))
                .flatMap(this::setDraftToProgress)
                .flatMapCompletable(uploadDraft1 -> getSplitsNotExistentPaths(
                    uploadDraft1.getSplitsToBeUploaded()).doOnSuccess(
                    splits -> Log.d("upload", "split_paths " + splits))
                    .flatMapCompletable(splits -> uploaderService.uploadSplits(uploadDraft1, splits)
                        .filter(waitingUploadConfirmation())
                        .doOnNext(uploadDraft2 -> Log.d("upload",
                            "upload complete " + uploadDraft2.toString()))
                        .flatMapCompletable(uploadedDraft -> setPending(uploadedDraft).doOnSuccess(
                            uploadDraft2 -> Log.d("upload",
                                "setted PENDING " + uploadDraft2.toString()))
                            .flatMap(this::getDraftStatus)
                            .doOnSuccess(uploadDraft2 -> Log.d("upload",
                                "got status " + uploadDraft2.toString()))
                            .toCompletable())));
          }
          return Completable.complete();
        });
  }

  @NotNull
  private Function<UploadDraft, SingleSource<? extends UploadDraft>> handleHasMetadataResult() {
    return metadataInfoDraft -> {
      if (metadataInfoDraft.getStatus()
          .equals(UploadDraft.Status.NO_META_DATA)) {
        return observeMetadataFilling(metadataInfoDraft).flatMap(this::setMetadata);
      } else if (metadataInfoDraft.getStatus()
          .equals(UploadDraft.Status.SET_STATUS_TO_DRAFT)) {
        return Single.just(metadataInfoDraft);
      }
      return Single.error(new IllegalStateException(metadataInfoDraft.getStatus()
          .name()));
    };
  }

  private Single<UploadDraft> observeMetadataFilling(UploadDraft metadataInfoDraft) {
    return draftPersistence.getDrafts()
        .flatMap(drafts -> Observable.fromIterable(drafts)
            .filter(draft -> draft.getStatus()
                .equals(UploadDraft.Status.META_DATA_ADDED) && draft.getMd5()
                .equals(metadataInfoDraft.getMd5()))
            .toList()
            .toObservable())
        .filter(uploadDrafts -> !uploadDrafts.isEmpty())
        .firstOrError()
        .map(uploadDrafts -> uploadDrafts.get(0));
  }

  @NotNull private Predicate<UploadDraft> waitingUploadConfirmation() {
    return uploadedDraft -> uploadedDraft.getStatus()
        .equals(UploadDraft.Status.WAITING_UPLOAD_CONFIRMATION);
  }

  private Single<UploadDraft> uploadFiles(UploadDraft draftDraft) {
    return uploaderService.uploadFiles(draftDraft)
        .singleOrError()
        .flatMap(uploadDraft -> draftPersistence.save(uploadDraft)
            .toSingleDefault(uploadDraft));
  }

  private Single<UploadDraft> setMetadata(UploadDraft draft) {
    return uploaderService.setDraftMetadata(draft)
        .singleOrError()
        .flatMap(metadataDraft -> draftPersistence.save(metadataDraft)
            .toSingleDefault(metadataDraft));
  }

  private Single<UploadDraft> setDraft(UploadDraft metaDataDraft, UploadDraft.Status status) {
    return uploaderService.setDraftStatus(metaDataDraft, DraftStatus.DRAFT)
        .singleOrError()
        .flatMap(draftDraft -> draftPersistence.save(
            new UploadDraft(status, draftDraft.getInstalledApp(), draftDraft.getMd5(),
                draftDraft.getDraftId(), draftDraft.getMetadata()))
            .toSingleDefault(draftDraft));
  }

  private Single<UploadDraft> setDraftToProgress(UploadDraft draft) {
    return draftPersistence.save(
        new UploadDraft(UploadDraft.Status.PROGRESS, draft.getInstalledApp(), draft.getMd5(),
            draft.getDraftId(), draft.getMetadata()))
        .toSingleDefault(draft);
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

  @SuppressLint("CheckResult") private void handleCompletedStatus() {
    draftPersistence.getDrafts()
        .distinctUntilChanged((previous, current) -> !draftPersistenceHasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(draft -> draft.getStatus()
            .equals(UploadDraft.Status.COMPLETED) || draft.getStatus()
            .equals(UploadDraft.Status.DUPLICATE))
        .flatMapCompletable(upload -> appUploadStatusPersistence.save(
            new AppUploadStatus(upload.getMd5(), upload.getInstalledApp()
                .getPackageName(), AppUploadStatus.Status.IN_STORE, String.valueOf(
                upload.getInstalledApp()
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

  @SuppressLint("CheckResult") public void fillAppUploadStatusPersistence() {
    appUploadStatusManager.getUncheckedApps()
        .toObservable()
        .flatMapIterable(apps -> apps)
        .flatMapSingle(installedApp -> md5Calculator.calculate(installedApp.getApkPath())
            .map(md5 -> new AppUploadStatus(md5, installedApp.getPackageName(),
                AppUploadStatus.Status.UNKNOWN, String.valueOf(installedApp.getVersionCode()))))
        .toList()
        .flatMapCompletable(installedApps -> appUploadStatusPersistence.saveAll(installedApps))
        .subscribeOn(Schedulers.computation())
        .subscribe();
  }

  @SuppressLint("CheckResult") private void checkAppUploadStatus() {
    accountProvider.getAccount()
        .switchMap(account -> {
          if (account.isLoggedIn() && account.hasStore()) {
            return appUploadStatusPersistence.getAppsUnknownUploadStatus()
                .distinctUntilChanged(
                    (previous, current) -> !appsPersistenceHasChanged(previous, current))
                .flatMapSingle(uploadStatuses -> Observable.fromIterable(uploadStatuses)
                    .map(appUploadStatus -> new AppUploadStatus(appUploadStatus.getMd5(),
                        appUploadStatus.getPackageName(), AppUploadStatus.Status.PROCESSING,
                        appUploadStatus.getVercode()))
                    .toList()
                    .flatMap(uploadStatuses1 -> appUploadStatusPersistence.saveAll(uploadStatuses1)
                        .andThen(Single.just(uploadStatuses))))
                .flatMap(uploadStatuses -> Observable.just(uploadStatuses)
                    .doOnNext(appUploadStatuses -> Log.d("nzxt", "" + appUploadStatuses.size()))
                    .flatMapSingle(appUploadStatuses -> Observable.fromIterable(appUploadStatuses)
                        .toList())
                    .flatMap(appUploadStatuses -> appUploadStatusManager.getApks(appUploadStatuses))
                    .flatMapCompletable(
                        appUploadStatuses -> appUploadStatusPersistence.saveAll(appUploadStatuses))
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

    for (AppUploadStatus previous : previousList) {
      AppUploadStatus current = currentList.get(previousList.indexOf(previous));
      if (!previous.getStatus()
          .equals(current.getStatus())) {
        return true;
      }
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
