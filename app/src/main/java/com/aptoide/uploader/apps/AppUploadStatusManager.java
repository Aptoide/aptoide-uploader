package com.aptoide.uploader.apps;

import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.network.RetrofitAppsUploadStatusService;
import com.aptoide.uploader.apps.network.RetrofitStoreService;
import io.reactivex.Observable;
import io.reactivex.Single;
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

  public Single<List<InstalledApp>> getNonSystemApps() {
    return installedAppsProvider.getInstalledApps()
        .flatMapObservable(apps -> Observable.fromIterable(apps))
        .filter(app -> !app.isSystem())
        .toList();
  }
}
