package com.aptoide.uploader.apps.persistence;

import android.util.Log;
import com.aptoide.uploader.apps.AppUploadStatus;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryAppUploadStatusPersistence implements AppUploadStatusPersistence {
  private final Map<String, AppUploadStatus> appUploadStatusMap;
  private final PublishSubject<List<AppUploadStatus>> appUploadStatusListSubject;
  private final Scheduler scheduler;

  public MemoryAppUploadStatusPersistence(Map<String, AppUploadStatus> uploadStatusMap,
      Scheduler scheduler) {
    this.appUploadStatusMap = uploadStatusMap;
    this.scheduler = scheduler;
    appUploadStatusListSubject = PublishSubject.create();
  }

  @Override public Observable<List<AppUploadStatus>> getAppsUploadStatus() {
    return appUploadStatusListSubject.startWith(
        new ArrayList<AppUploadStatus>(appUploadStatusMap.values()))
        .subscribeOn(scheduler);
  }

  @Override public Completable save(AppUploadStatus appUploadStatus) {
    return Completable.fromAction(() -> {
      appUploadStatusMap.put(appUploadStatus.getMd5(), appUploadStatus);
      appUploadStatusListSubject.onNext(new ArrayList<>(appUploadStatusMap.values()));
    })
        .subscribeOn(scheduler)
        .doOnError(throwable -> Log.e("ERROR Save", throwable.getMessage()));
  }

  @Override public Completable saveAll(List<AppUploadStatus> appUploadStatusList) {
    return Completable.fromAction(() -> {
      HashMap<String, AppUploadStatus> map = new HashMap<>();
      for (AppUploadStatus appUploadStatus : appUploadStatusList) {
        map.put(appUploadStatus.getMd5(), appUploadStatus);
      }
      appUploadStatusMap.putAll(map);
      appUploadStatusListSubject.onNext(new ArrayList<>(appUploadStatusMap.values()));
    })
        .subscribeOn(scheduler)
        .doOnError(throwable -> Log.e("ERROR Save", throwable.getMessage()));
  }

  @Override public Completable remove(String md5) {
    return Completable.fromAction(() -> appUploadStatusMap.remove(md5))
        .subscribeOn(scheduler)
        .doOnError(throwable -> Log.e("ERROR Remove", throwable.getMessage()));
  }

  @Override public Completable update(AppUploadStatus appUploadStatus, boolean isUploaded) {
    return save(appUploadStatus)
        //new AppUploadStatus(appUploadStatus.getMd5(), appUploadStatus.getPackageName(), isUploaded,
        //    appUploadStatus.getVercode()))
        .doOnError(throwable -> Log.e("ERROR Updating", throwable.getMessage()));
  }

  @Override public void clear() {

  }
}
