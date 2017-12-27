package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.UploaderService;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public class UploadManager {
  private final UploaderService uploaderService;
  private final UploaderPersistence persistence;
  private final Md5Calculator md5Calculator;

  public UploadManager(UploaderService uploaderService, UploaderPersistence persistence,
      Md5Calculator md5Calculator) {
    this.uploaderService = uploaderService;
    this.persistence = persistence;
    this.md5Calculator = md5Calculator;
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

  public void uploadAppToRepo() {
    uploaderService.uploadAppToRepo();
  }
}
