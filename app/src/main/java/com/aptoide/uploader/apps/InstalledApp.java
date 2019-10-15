package com.aptoide.uploader.apps;

import android.graphics.drawable.Drawable;

public class InstalledApp {

  private final Drawable icon;
  private final String name;
  private final boolean isSystem;
  private final String packageName;
  private final String apkPath;
  private final long installedDate;
  private final int versionCode;
  private boolean isUploaded;

  public InstalledApp(Drawable icon, String name, boolean isSystem, String packageName,
      String apkPath, long installedDate, int versionCode, boolean isUploaded) {
    this.icon = icon;
    this.name = name;
    this.isSystem = isSystem;
    this.packageName = packageName;
    this.apkPath = apkPath;
    this.installedDate = installedDate;
    this.versionCode = versionCode;
    this.isUploaded = isUploaded;
  }

  public Drawable getIcon() {
    return icon;
  }

  public String getName() {
    return name;
  }

  public boolean isSystem() {
    return isSystem;
  }

  public String getPackageName() {
    return packageName;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InstalledApp that = (InstalledApp) o;

    return packageName.equals(that.packageName);
  }

  @Override public int hashCode() {
    return packageName.hashCode();
  }

  public String getApkPath() {
    return apkPath;
  }

  public long getInstalledDate() {
    return installedDate;
  }

  public int getVersionCode() {
    return versionCode;
  }

  public boolean isUploaded() {
    return isUploaded;
  }

  public void setIsUploaded(boolean value) {
    isUploaded = value;
  }
}
