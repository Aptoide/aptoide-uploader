package com.aptoide.uploader.apps;

import io.reactivex.Single;
import java.util.List;

public class AppsManager {

  private final PackageProvider packageProvider;

  public AppsManager(PackageProvider packageProvider) {
    this.packageProvider = packageProvider;
  }

  public Single<List<App>> getStore() {
    return packageProvider.getInstalledApps();
  }
}
