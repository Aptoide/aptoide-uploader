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
  private final Obb obbMain;
  private final Obb obbPatch;

  public InstalledApp(Drawable icon, String name, boolean isSystem, String packageName,
      String apkPath, long installedDate, int versionCode, boolean isUploaded, Obb obbMain,
      Obb obbPatch) {
    this.icon = icon;
    this.name = name;
    this.isSystem = isSystem;
    this.packageName = packageName;
    this.apkPath = apkPath;
    this.installedDate = installedDate;
    this.versionCode = versionCode;
    this.isUploaded = isUploaded;
    this.obbMain = obbMain;
    this.obbPatch = obbPatch;
  }

  public String getObbMainPath() {
    if (obbMain != null) {
      return obbMain.getPath();
    }
    return null;
  }

  public String getObbMainMd5() {
    if (obbMain != null) {
      return obbMain.getMd5sum();
    }
    return null;
  }

  public String getObbMainFilename() {
    if (obbMain != null) {
      return obbMain.getFilename();
    }
    return null;
  }

  public String getObbPatchPath() {
    if (obbPatch != null) {
      return obbPatch.getPath();
    }
    return null;
  }

  public String getObbPatchMd5() {
    if (obbPatch != null) {
      return obbPatch.getMd5sum();
    }
    return null;
  }

  public String getObbPatchFilename() {
    if (obbPatch != null) {
      return obbPatch.getFilename();
    }
    return null;
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
