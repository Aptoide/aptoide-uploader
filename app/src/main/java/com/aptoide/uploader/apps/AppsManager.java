package com.aptoide.uploader.apps;

import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public class AppsManager {

  private final PackageProvider packageProvider;

  public AppsManager(PackageProvider packageProvider) {
    this.packageProvider = packageProvider;
  }

  public Single<List<App>> getApps() {
    return packageProvider.getInstalledApps()
        .flatMapObservable(apps -> Observable.fromIterable(apps))
        .filter(app -> !app.isSystem())
        .toList();
  }
}
