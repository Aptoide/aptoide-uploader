package com.aptoide.uploader.apps;

import io.reactivex.Completable;

public class UploadManager {

  private final UploaderService service;
  private final UploaderPersistence persistence;
  private final Md5Calculator md5Calculator;

  public UploadManager(UploaderService service, UploaderPersistence persistence,
      Md5Calculator md5Calculator) {
    this.service = service;
    this.persistence = persistence;
    this.md5Calculator = md5Calculator;
  }

  public Completable upload(String storeName, String language, App app) {
    return md5Calculator.calculate(app)
        .flatMapCompletable(
            md5 -> service.getUploadApp(md5, app.getPackageName(), language, storeName)
                .flatMapCompletable(uploadApp -> {
                  if (uploadApp.isUploaded()) {
                    if (!uploadApp.hasProposedData()) {
                      return persistence.save(uploadApp, UploadApp.Status.UPLOADED);
                    }
                  }
                  return persistence.save(uploadApp, UploadApp.Status.PENDING);
                }));
  }
}
