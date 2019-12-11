package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.List;

public class InstalledApp {

  private final String name;
  private final boolean isSystem;
  private final String packageName;
  private final String apkPath;
  private final long installedDate;
  private final int versionCode;
  private boolean isUploaded;
  private final Obb obbMain;
  private final Obb obbPatch;
  private ApplicationInfo appInfo;

  public InstalledApp(ApplicationInfo appInfo, String name, boolean isSystem, String packageName,
      String apkPath, long installedDate, int versionCode, boolean isUploaded, Obb obbMain,
      Obb obbPatch) {
    this.appInfo = appInfo;
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

  public ApplicationInfo getAppInfo() {
    return appInfo;
  }

  public List<String> getObbList() {
    List<String> list = new ArrayList<>();
    if (getObbMainPath() != null) {
      list.add(getObbMainPath());
    }
    if (getObbPatchPath() != null) {
      list.add(getObbPatchPath());
    }
    return list;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) public List<FileToUpload> getSplits() {
    List<FileToUpload> list = new ArrayList<>();
    if (getAppInfo().splitSourceDirs != null) {
      if (getAppInfo().splitSourceDirs.length != 0) {
        for (int i = 0; i < getAppInfo().splitSourceDirs.length; i++) {
          list.add(new FileToUpload(getAppInfo().splitSourceDirs[i], FileType.SPLIT));
        }
      }
    }
    return list;
  }

  public List<FileToUpload> getRegularApkFiles() {
    List<FileToUpload> list = new ArrayList<>();
    list.add(new FileToUpload(getApkPath(), FileType.BASE));
    if (getObbMainPath() != null) {
      list.add(new FileToUpload(getObbMainPath(), FileType.OBB_MAIN));
    }
    if (getObbPatchPath() != null) {
      list.add(new FileToUpload(getObbPatchPath(), FileType.OBB_PATCH));
    }
    return list;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) public List<FileToUpload> getAppBundleFiles() {
    List<FileToUpload> list = new ArrayList<>();
    list.add(new FileToUpload(getApkPath(), FileType.BASE));
    if (getSplits() != null) {
      list.addAll(getSplits());
    }
    return list;
  }

  public class FileToUpload {
    private String path;
    private FileType type;

    public FileToUpload(String path, FileType type) {
      this.path = path;
      this.type = type;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public FileType getType() {
      return type;
    }

    public void setType(FileType type) {
      this.type = type;
    }
  }

  public enum FileType {
    BASE, OBB_MAIN, OBB_PATCH, SPLIT
  }
}
