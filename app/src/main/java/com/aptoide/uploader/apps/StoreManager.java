package com.aptoide.uploader.apps;

import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public class StoreManager {

  private final PackageProvider packageProvider;
  private final StoreNameProvider storeNameProvider;

  public StoreManager(PackageProvider packageProvider, StoreNameProvider storeNameProvider) {
    this.packageProvider = packageProvider;
    this.storeNameProvider = storeNameProvider;
  }

  public Single<Store> getStore() {
    return Single.zip(getNonSystemApps(), storeNameProvider.getStoreName(),
        (apps, storeName) -> new Store(storeName, apps));
  }

  private Single<List<App>> getNonSystemApps() {
    return packageProvider.getInstalledApps()
        .flatMapObservable(apps -> Observable.fromIterable(apps))
        .filter(app -> !app.isSystem())
        .toList();
  }
}
