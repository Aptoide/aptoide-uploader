package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public class PackageManagerProvider implements PackageProvider {

  private final PackageManager packageManager;

  public PackageManagerProvider(PackageManager packageManager) {
    this.packageManager = packageManager;
  }

  @Override public Single<List<App>> getInstalledApps() {
    return Observable.fromIterable(
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
        .filter(applicationInfo -> applicationInfo.packageName != null)
        .map(applicationInfo -> new App(
            "android.resource://" + applicationInfo.packageName + "/" + applicationInfo.icon,
            applicationInfo.loadLabel(packageManager)
                .toString(), (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0,
            applicationInfo.packageName))
        .toList();
  }
}
