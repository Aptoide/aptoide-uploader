package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.AppUploadStatus;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface AppUploadStatusPersistence {

  Observable<List<AppUploadStatus>> getAppsUploadStatus();

  Observable<List<AppUploadStatus>> getAppsUnknownUploadStatus();

  Observable<List<AppUploadStatus>> getUploadedApps();

  Completable save(AppUploadStatus appUploadStatus);

  Completable saveAll(List<AppUploadStatus> appUploadStatusList);

  boolean isUploadedVersion(String installedPackageName, int versionCode);

  Completable remove(AppUploadStatus appUploadStatus);
}
