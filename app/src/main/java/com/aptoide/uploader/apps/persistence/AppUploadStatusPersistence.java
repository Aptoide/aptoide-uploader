package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.AppUploadStatus;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface AppUploadStatusPersistence {

  Observable<List<AppUploadStatus>> getAppsUploadStatus();

  Completable save(AppUploadStatus appUploadStatus);

  Completable saveAll(List<AppUploadStatus> appUploadStatusList);

  Completable remove(String md5);

  Completable update(AppUploadStatus appUploadStatus, boolean isUploaded);

  void clear();
}
