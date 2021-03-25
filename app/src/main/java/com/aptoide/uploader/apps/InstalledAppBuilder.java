package com.aptoide.uploader.apps;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import java.io.File;
import java.util.Arrays;

public class InstalledAppBuilder {
  private final InstalledApp installedApp;

  public InstalledAppBuilder(PackageInfo packageInfo, PackageManager packageManager) {
    installedApp = new InstalledApp(packageInfo.applicationInfo,
        packageInfo.packageName + packageInfo.versionCode, packageInfo.packageName,
        packageInfo.applicationInfo.loadLabel(packageManager)
            .toString(), packageInfo.versionName, packageInfo.versionCode,
        (packageInfo.applicationInfo.flags & packageInfo.applicationInfo.FLAG_SYSTEM) != 0,
        packageInfo.applicationInfo.sourceDir,
        "android.resource://" + packageInfo.packageName + "/" + packageInfo.applicationInfo.icon,
        packageInfo.lastUpdateTime, false, getMainObb(packageInfo.packageName),
        getPatchObb(packageInfo.packageName));
  }

  public Obb getMainObb(String packageName) {
    String sdcard = Environment.getExternalStorageDirectory()
        .getAbsolutePath();
    File obbDir = new File(sdcard + "/Android/obb/" + packageName + "/");
    if (obbDir.isDirectory()) {
      File[] files = obbDir.listFiles();

      if (files != null) {
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
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

  public Obb getPatchObb(String packageName) {
    String sdcard = Environment.getExternalStorageDirectory()
        .getAbsolutePath();
    File obbDir = new File(sdcard + "/Android/obb/" + packageName + "/");
    if (obbDir.isDirectory()) {
      File[] files = obbDir.listFiles();
      if (files != null) {
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
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

  public InstalledApp getInstalledApp() {
    return installedApp;
  }
}
