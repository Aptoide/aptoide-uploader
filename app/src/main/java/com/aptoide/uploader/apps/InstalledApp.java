package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Entity(tableName = "Installed") public class InstalledApp {
  @PrimaryKey @NonNull private String packageAndVersionCode;
  private String packageName;
  private String name;
  private String versionName;
  private int versionCode;
  private boolean isSystem;
  private String apkPath;
  private String iconPath;
  private long installedDate;
  @Ignore private List<Obb> obbList = new ArrayList<>();
  @Ignore private ApplicationInfo appInfo;

  public InstalledApp() {
  }

  public InstalledApp(ApplicationInfo applicationInfo, String packageAndVersionCode,
      String packageName, String name, String versionName, int versionCode, boolean isSystem,
      String apkPath, String iconPath, long lastUpdateTime, Obb obbMain, Obb obbPatch) {
    this.appInfo = applicationInfo;
    this.packageAndVersionCode = packageAndVersionCode;
    this.packageName = packageName;
    this.name = name;
    this.versionName = versionName;
    this.versionCode = versionCode;
    this.isSystem = isSystem;
    this.apkPath = apkPath;
    this.iconPath = iconPath;
    this.installedDate = lastUpdateTime;
    this.obbList.add(0, obbMain);
    this.obbList.add(1, obbPatch);
  }

  @NonNull public String getPackageAndVersionCode() {
    return packageAndVersionCode;
  }

  public void setPackageAndVersionCode(@NotNull String packageAndVersionCode) {
    this.packageAndVersionCode = packageAndVersionCode;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersionName() {
    return versionName;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public int getVersionCode() {
    return versionCode;
  }

  public void setVersionCode(int versionCode) {
    this.versionCode = versionCode;
  }

  public boolean isSystem() {
    return isSystem;
  }

  public void setIsSystem(boolean systemApp) {
    this.isSystem = systemApp;
  }

  public String getApkPath() {
    return apkPath;
  }

  public void setApkPath(String apkPath) {
    this.apkPath = apkPath;
  }

  public String getIconPath() {
    return iconPath;
  }

  public void setIconPath(String icon) {
    this.iconPath = icon;
  }

  public long getInstalledDate() {
    return installedDate;
  }

  public void setInstalledDate(long installedDate) {
    this.installedDate = installedDate;
  }

  public Obb getObbMain() {
    return obbList.get(0);
  }

  public Obb getObbPatch() {
    return obbList.get(1);
  }

  public ApplicationInfo getAppInfo() {
    return appInfo;
  }

  public void setAppInfo(ApplicationInfo appInfo) {
    this.appInfo = appInfo;
  }

  public String getObbMainPath() {
    if (obbList.get(0) != null) {
      return obbList.get(0)
          .getPath();
    }
    return null;
  }

  public String getObbMainMd5() {
    if (obbList.get(0) != null) {
      return obbList.get(0)
          .getMd5sum();
    }
    return null;
  }

  public String getObbMainFilename() {
    if (obbList.get(0) != null) {
      return obbList.get(0)
          .getFilename();
    }
    return null;
  }

  public String getObbPatchPath() {
    if (obbList.get(1) != null) {
      return obbList.get(1)
          .getPath();
    }
    return null;
  }

  public String getObbPatchMd5() {
    if (obbList.get(1) != null) {
      return obbList.get(1)
          .getMd5sum();
    }
    return null;
  }

  public String getObbPatchFilename() {
    if (obbList.get(1) != null) {
      return obbList.get(1)
          .getFilename();
    }
    return null;
  }

  @Override public int hashCode() {
    return packageName.hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InstalledApp that = (InstalledApp) o;
    return packageName.equals(that.packageName);
  }

  @Override public String toString() {
    return "InstalledApp{" + "packageName='" + packageName + '\'' + '}';
  }

  public List<Obb> getObbList() {
    return this.obbList;
  }

  public void setObbList(List<Obb> obbList) {
    this.obbList = obbList;
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
    List<FileToUpload> splits = getSplits();
    list.addAll(splits);
    return list;
  }

  public enum FileType {
    BASE, OBB_MAIN, OBB_PATCH, SPLIT
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
}
