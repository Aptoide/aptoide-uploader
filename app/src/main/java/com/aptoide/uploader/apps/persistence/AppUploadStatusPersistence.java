package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.AppUploadStatus;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface AppUploadStatusPersistence {

  Observable<List<AppUploadStatus>> getAppsUploadStatus();

  Observable<List<AppUploadStatus>> getAppsUnknownUploadStatus();

  Completable save(AppUploadStatus appUploadStatus);

  Completable saveAll(List<AppUploadStatus> appUploadStatusList);

  Completable remove(AppUploadStatus appUploadStatus);
}
