package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class PackageManagerInstalledAppsProvider implements InstalledAppsProvider {

  private final PackageManager packageManager;
  private PackageInfo packageInfo;

  public PackageManagerInstalledAppsProvider(PackageManager packageManager) {
    this.packageManager = packageManager;
  }

  @Override public Single<List<InstalledApp>> getInstalledApps() {
    return Observable.fromIterable(
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
        .filter(applicationInfo -> applicationInfo.packageName != null)
        .map(applicationInfo -> {
          packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
          return new InstalledApp(
              "android.resource://" + applicationInfo.packageName + "/" + applicationInfo.icon,
              applicationInfo.loadLabel(packageManager)
                  .toString(), (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1,
              applicationInfo.packageName, applicationInfo.sourceDir, packageInfo.firstInstallTime,
              packageInfo.versionCode, false);
        })
        .toList()
        .observeOn(Schedulers.io());
  }
}
