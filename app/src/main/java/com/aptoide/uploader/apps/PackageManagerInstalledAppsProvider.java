package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.io.File;
import java.util.List;

public class PackageManagerInstalledAppsProvider implements InstalledAppsProvider {

  private final PackageManager packageManager;
  private PackageInfo packageInfo;

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
          packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
          return new InstalledApp(packageInfo.applicationInfo,
              applicationInfo.loadLabel(packageManager)
                  .toString(), (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1,
              applicationInfo.packageName, applicationInfo.sourceDir, packageInfo.lastUpdateTime,
              packageInfo.versionCode, false, getMainObb(applicationInfo.packageName),
              getPatchObb(applicationInfo.packageName));
        })
        .toList()
        .subscribeOn(scheduler);
  }

  private Obb getMainObb(String packageName) {
    String sdcard = Environment.getExternalStorageDirectory()
        .getAbsolutePath();
    File obbDir = new File(sdcard + "/Android/obb/" + packageName + "/");
    if (obbDir.isDirectory()) {
      File[] files = obbDir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.getName()
              .contains("main") && !file.getName()
              .contains("--downloading")) {
            String obbMainPath = file.getAbsolutePath();
            return new Obb(obbMainPath.substring(obbMainPath.lastIndexOf("/") + 1), null,
                obbMainPath);
          }
        }
      }
    }
    return null;
  }

  private Obb getPatchObb(String packageName) {
    String sdcard = Environment.getExternalStorageDirectory()
        .getAbsolutePath();
    File obbDir = new File(sdcard + "/Android/obb/" + packageName + "/");
    if (obbDir.isDirectory()) {
      File[] files = obbDir.listFiles();
      if (files != null) {
        for (File file : files) {
          String arr[] = file.getName()
              .split("\\.", 2);
          if (arr[0].equals("patch") && !file.getName()
              .contains("--downloading")) {
            String obbPatchPath = file.getAbsolutePath();
            return new Obb(obbPatchPath.substring(obbPatchPath.lastIndexOf("/") + 1), null,
                obbPatchPath);
          }
        }
      }
    }
    return null;
  }
}
