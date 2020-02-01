package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.RetrofitAppsUploadStatusService;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;

public class AppUploadStatusManager {

  private final StoreNameProvider storeNameProvider;
  private final RetrofitAppsUploadStatusService retrofitAppsUploadStatusService;
  private final InstalledAppsProvider installedAppsProvider;
  private final AppUploadStatusPersistence appUploadStatusPersistence;

  public AppUploadStatusManager(StoreNameProvider storeNameProvider,
      RetrofitAppsUploadStatusService retrofitAppsUploadStatusService,
      InstalledAppsProvider installedAppsProvider,
      AppUploadStatusPersistence appUploadStatusPersistence) {
    this.storeNameProvider = storeNameProvider;
    this.retrofitAppsUploadStatusService = retrofitAppsUploadStatusService;
    this.installedAppsProvider = installedAppsProvider;
    this.appUploadStatusPersistence = appUploadStatusPersistence;
  }

  public Observable<List<AppUploadStatus>> getApks(List<AppUploadStatus> appUploadStatuses) {
    List<List<AppUploadStatus>> partitions = partitionLists(appUploadStatuses);
    return storeNameProvider.getStoreName()
        .toObservable()
        .flatMap(storeName -> Observable.fromIterable(partitions)
            .concatMap(subList -> retrofitAppsUploadStatusService.getApks(subList, storeName)));
  }

  public List<List<AppUploadStatus>> partitionLists(List<AppUploadStatus> list) {
    int partitionSize = 20;
    List<List<AppUploadStatus>> partitions = new ArrayList<>();
    for (int i = 0; i < list.size(); i += partitionSize) {
      partitions.add(list.subList(i, Math.min(i + partitionSize, list.size())));
    }
    return partitions;
  }

  public Single<List<InstalledApp>> getUncheckedApps() {
    return installedAppsProvider.getInstalledApps()
        .flatMapObservable(apps -> Observable.fromIterable(apps))
        .filter(app -> !app.isSystem())
        .flatMap(app -> appUploadStatusPersistence.getAppsUploadStatus()
            .firstOrError()
            .flatMapObservable(appUploadStatuses -> {
              for (AppUploadStatus appStatus : appUploadStatuses) {
                String newPackageName = app.getPackageName();
                String oldPackageName = appStatus.getPackageName();
                int newVersionCode = app.getVersionCode();
                int oldVersionCode;
                try {
                  oldVersionCode = Integer.parseInt(appStatus.getVercode());
                } catch (NumberFormatException e) {
                  oldVersionCode = 0;
                }
                if (newPackageName.equals(oldPackageName)) {
                  if (newVersionCode != oldVersionCode) {
                    //new version of the app
                    return appUploadStatusPersistence.remove(appStatus)
                        .andThen(Observable.just(app));
                  } else {
                    //same app
                    return Observable.empty();
                  }
                }
              }
              //new app
              return Observable.just(app);
            }))
        .toList();
  }
}
