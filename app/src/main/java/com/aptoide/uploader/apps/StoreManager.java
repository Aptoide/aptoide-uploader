package com.aptoide.uploader.apps;

import com.aptoide.uploader.account.AptoideAccountManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.util.List;

public class StoreManager {

  private final InstalledAppsProvider installedAppsProvider;
  private final StoreNameProvider storeNameProvider;
  private final UploadManager uploadManager;
  private final LanguageManager languageManager;
  private final AptoideAccountManager accountManager;
  private Scheduler scheduler;
  private ServiceBackgroundService serviceBackgroundService;

  public StoreManager(InstalledAppsProvider installedAppsProvider,
      StoreNameProvider storeNameProvider, UploadManager uploadManager,
      LanguageManager languageManager, AptoideAccountManager accountManager, Scheduler scheduler,
      ServiceBackgroundService serviceBackgroundService) {
    this.installedAppsProvider = installedAppsProvider;
    this.storeNameProvider = storeNameProvider;
    this.uploadManager = uploadManager;
    this.languageManager = languageManager;
    this.accountManager = accountManager;
    this.scheduler = scheduler;
    this.serviceBackgroundService = serviceBackgroundService;
  }

  public Single<Store> getStore() {
    return Single.zip(installedAppsProvider.getNonSystemInstalledApps(),
        storeNameProvider.getStoreName(), (apps, storeName) -> new Store(storeName, apps))
        .subscribeOn(scheduler);
  }

  public Completable upload(List<InstalledApp> apps) {
    return storeNameProvider.getStoreName()
        .flatMapCompletable(storeName -> languageManager.getCurrentLanguageCode()
            .flatMapCompletable(languageCode -> Observable.fromIterable(apps)
                .flatMapCompletable(
                    app -> installedAppsProvider.getInstalledApp(app.getPackageName())
                        .flatMapCompletable(
                            installedApp -> uploadManager.upload(storeName, languageCode,
                                installedApp)))));
  }

  public Completable logout() {
    return accountManager.logout();
  }
}
