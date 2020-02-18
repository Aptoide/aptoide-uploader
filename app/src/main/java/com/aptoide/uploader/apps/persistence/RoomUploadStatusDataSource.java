package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.AppUploadStatus;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public class RoomUploadStatusDataSource implements AppUploadStatusPersistence {

  private final AppUploadStatusDao appUploadStatusDao;

  public RoomUploadStatusDataSource(AppUploadStatusDao appUploadStatusDao) {
    this.appUploadStatusDao = appUploadStatusDao;
  }

  @Override public Observable<List<AppUploadStatus>> getAppsUploadStatus() {
    return appUploadStatusDao.getAppUploadsStatuses();
  }

  @Override public Observable<List<AppUploadStatus>> getAppsUnknownUploadStatus() {
    return appUploadStatusDao.getAppsUnknownUploadStatus(AppUploadStatus.Status.UNKNOWN.getCode());
  }

  @Override public Completable save(AppUploadStatus appUploadStatus) {
    return appUploadStatusDao.save(appUploadStatus);
  }

  @Override public Completable saveAll(List<AppUploadStatus> appUploadStatusList) {
    return appUploadStatusDao.saveAll(appUploadStatusList);
  }

  @Override public Completable remove(AppUploadStatus packageName) {
    return appUploadStatusDao.remove(packageName);
  }
}
