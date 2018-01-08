package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.UploaderService;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import com.aptoide.uploader.upload.BackgroundService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public class UploadManager {
  private final UploaderService uploaderService;
  private final UploaderPersistence persistence;
  private final Md5Calculator md5Calculator;
  private final BackgroundService backgroundService;

  public UploadManager(UploaderService uploaderService, UploaderPersistence persistence,
      Md5Calculator md5Calculator, BackgroundService backgroundService) {
    this.uploaderService = uploaderService;
    this.persistence = persistence;
    this.md5Calculator = md5Calculator;
    this.backgroundService = backgroundService;
  }

  public Completable upload(String storeName, String language, InstalledApp app) {
    return md5Calculator.calculate(app)
        .flatMapCompletable((md5 -> uploaderService.getAppUpload(md5, language, storeName, app)
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

  public Observable<Upload> uploadAppToRepo(Upload upload) {
    return uploaderService.uploadAppToRepo(upload);
  }

  public void start() {
    handleBackgroundService();
    dispatchUploads();
  }

  private void handleBackgroundService() {
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
        });
  }

  private void dispatchUploads() {
    persistence.getUploads()
        .distinctUntilChanged((previous, current) -> !hasChanged(previous, current))
        .flatMapIterable(uploads -> uploads)
        .filter(upload -> upload.getStatus()
            .equals(Upload.Status.PENDING))
        .flatMap(upload -> uploadAppToRepo(upload))
        .flatMapCompletable(upload -> persistence.save(upload))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe();
  }

  private boolean hasChanged(List<Upload> previousList, List<Upload> currentList) {
    for (Upload previous : previousList) {
      Upload current = currentList.get(currentList.indexOf(previous));
      if (!previous.getStatus()
          .equals(current.getStatus())) {
        return true;
      }
    }
    return !previousList.equals(currentList);
  }
}
