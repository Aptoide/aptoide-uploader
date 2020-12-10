package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PackageManagerInstalledAppsProvider implements InstalledAppsProvider {

  private final PackageManager packageManager;
  private Scheduler scheduler;

  public PackageManagerInstalledAppsProvider(PackageManager packageManager, Scheduler scheduler) {
    this.packageManager = packageManager;
    this.scheduler = scheduler;
  }

  @Override public Single<List<InstalledApp>> getInstalledApps() {
    return Observable.fromIterable(
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
        .filter(applicationInfo -> applicationInfo.packageName != null)
        .map(applicationInfo -> {
          PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
          return new InstalledApp(packageInfo,packageManager);
        })
        .toList()
        .doOnSuccess(installedApps -> Log.d("APP-85", String.valueOf(installedApps.size())))
        .subscribeOn(scheduler);
  }

  @Override public Single<InstalledApp> getInstalledApp(String packageName) {
    return Single.fromCallable(
        () -> packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA))
        .map(info -> {
          PackageInfo packageInfo = packageManager.getPackageInfo(info.packageName, 0);
          return new InstalledApp(packageInfo,packageManager);
        });
  }
}
