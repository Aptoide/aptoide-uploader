package com.aptoide.uploader.apps;

import io.reactivex.Observable;
import java.util.List;

/**
 * Created by pedroribeiro on 10/11/17.
 */

public class AppsManager {

  private final PackageProvider packageProvider;

  public AppsManager(PackageProvider packageProvider) {
    this.packageProvider = packageProvider;
  }

  public Observable<List<App>> getInstalledApps() {
    return packageProvider.getInstalledApps();
  }
}
