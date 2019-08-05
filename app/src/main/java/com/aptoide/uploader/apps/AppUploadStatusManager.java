package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.ApksResponse;
import com.aptoide.uploader.apps.network.RetrofitAppsUploadStatusService;
import com.aptoide.uploader.apps.network.RetrofitStoreService;
import com.aptoide.uploader.apps.network.RetryWithDelay;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;

public class AppUploadStatusManager {

  private final StoreNameProvider storeNameProvider;
  private final RetrofitStoreService retrofitStoreService;
  private final RetrofitAppsUploadStatusService retrofitAppsUploadStatusService;
  private final InstalledAppsProvider installedAppsProvider;

  public AppUploadStatusManager(StoreNameProvider storeNameProvider,
      RetrofitStoreService retrofitStoreService,
      RetrofitAppsUploadStatusService retrofitAppsUploadStatusService,
      InstalledAppsProvider installedAppsProvider) {
    this.storeNameProvider = storeNameProvider;
    this.retrofitStoreService = retrofitStoreService;
    this.retrofitAppsUploadStatusService = retrofitAppsUploadStatusService;
    this.installedAppsProvider = installedAppsProvider;
  }

  public Observable<List<AppUploadStatus>> getApks(List<String> md5List) {
    return storeNameProvider.getStoreName()
        .flatMapObservable(
            storeName -> retrofitAppsUploadStatusService.getApks(md5List, storeName));
  }

  public Observable<ApksResponse> checkUploadStatus(String md5) {
    return storeNameProvider.getStoreName()
        .flatMapObservable(storeName -> {
          List<String> md5List = new ArrayList<>();
          md5List.add(md5);
          return retrofitStoreService.getApks(md5List, storeName);
        })
        .flatMap(apksResponse -> {
          if (apksResponse.getList()
              .isEmpty()) {
            throw new RuntimeException();
          }
          return Observable.just(apksResponse);
        })
        .retryWhen(new RetryWithDelay(3, 2000));
  }

  public Single<List<InstalledApp>> getNonSystemApps() {
    return installedAppsProvider.getInstalledApps()
        .flatMapObservable(apps -> Observable.fromIterable(apps))
        .filter(app -> !app.isSystem())
        .toList();
  }
}
