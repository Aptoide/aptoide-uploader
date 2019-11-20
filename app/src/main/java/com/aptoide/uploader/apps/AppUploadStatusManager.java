package com.aptoide.uploader.apps;

import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.network.GetApksRetryException;
import com.aptoide.uploader.apps.network.RetrofitAppsUploadStatusService;
import com.aptoide.uploader.apps.network.RetrofitStoreService;
import com.aptoide.uploader.apps.network.RetryWithDelay;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppUploadStatusManager {

  private final StoreNameProvider storeNameProvider;
  private final RetrofitStoreService retrofitStoreService;
  private final RetrofitAppsUploadStatusService retrofitAppsUploadStatusService;
  private final InstalledAppsProvider installedAppsProvider;
  private final UploaderAnalytics uploaderAnalytics;

  public AppUploadStatusManager(StoreNameProvider storeNameProvider,
      RetrofitStoreService retrofitStoreService,
      RetrofitAppsUploadStatusService retrofitAppsUploadStatusService,
      InstalledAppsProvider installedAppsProvider, UploaderAnalytics uploaderAnalytics) {
    this.storeNameProvider = storeNameProvider;
    this.retrofitStoreService = retrofitStoreService;
    this.retrofitAppsUploadStatusService = retrofitAppsUploadStatusService;
    this.installedAppsProvider = installedAppsProvider;
    this.uploaderAnalytics = uploaderAnalytics;
  }

  public Observable<List<AppUploadStatus>> getApks(List<String> md5List) {
    List<List<String>> partitions = partitionLists(md5List);
    return storeNameProvider.getStoreName()
        .toObservable()
        .flatMap(storeName -> Observable.fromIterable(partitions)
            .concatMap(subList -> retrofitAppsUploadStatusService.getApks(subList, storeName)));
  }

  public List<List<String>> partitionLists(List<String> list) {
    int partitionSize = 20;
    List<List<String>> partitions = new ArrayList<>();
    for (int i = 0; i < list.size(); i += partitionSize) {
      partitions.add(list.subList(i, Math.min(i + partitionSize, list.size())));
    }
    return partitions;
  }

  public Observable<Upload> checkUploadStatus(Upload upload) {
    return storeNameProvider.getStoreName()
        .flatMapObservable(storeName -> {
          List<String> md5List = new ArrayList<>();
          md5List.add(upload.getMd5());
          return retrofitStoreService.getApks(md5List, storeName);
        })
        .flatMap(apksResponse -> {
          if (apksResponse != null && apksResponse.getErrors() != null) {
            uploaderAnalytics.sendUploadCompleteEvent("fail", "Check if in Store",
                apksResponse.getErrors()
                    .getCode(), apksResponse.getErrors()
                    .getDescription());
            throw new IOException();
          }
          uploaderAnalytics.sendUploadCompleteEvent("success", "Check if in Store", null, null);
          upload.setStatus(Upload.Status.COMPLETED);
          return Observable.just(upload);
        })
        .retryWhen(new RetryWithDelay(3))
        .onErrorReturn(throwable -> {
          if (throwable instanceof GetApksRetryException) {
            upload.setStatus(Upload.Status.FAILED);
            return upload;
          }
          return null;
        });
  }

  public Single<List<InstalledApp>> getNonSystemApps() {
    return installedAppsProvider.getInstalledApps()
        .flatMapObservable(apps -> Observable.fromIterable(apps))
        .filter(app -> !app.isSystem())
        .toList();
  }
}
