package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Entity(tableName = "installed") public class InstalledApp {
  @Ignore public static final int STATUS_COMPLETED = 4;
  @PrimaryKey @NonNull private String packageAndVersionCode;
  private String packageName;
  private  String name;
  private String versionName;
  private int versionCode;
  private boolean isSystemApp;
  private String apkPath;
  private String iconPath;
  private long installedDate;
  @Ignore private Obb obbMain;
  @Ignore private Obb obbPatch;
  private boolean isUploaded;
  private int status;
  @Ignore private ApplicationInfo appInfo;

  public InstalledApp(){
  }

  public InstalledApp(PackageInfo packageInfo, PackageManager packageManager) {
    setAppInfo(packageInfo.applicationInfo);
    setPackageAndVersionCode(packageInfo.packageName + packageInfo.versionCode);
    setPackageName(packageInfo.packageName);
    setName(appInfo.loadLabel(packageManager).toString());
    setVersionName(packageInfo.versionName);
    setVersionCode(packageInfo.versionCode);
    setIsSystemApp((appInfo.flags & appInfo.FLAG_SYSTEM) != 0);
    setApkPath(appInfo.sourceDir);
    setIconPath("android.resource://"+ packageInfo.packageName+ "/"+ packageInfo.applicationInfo.icon);
    setInstalledDate(packageInfo.lastUpdateTime);
    setIsUploaded(false);
    setObbMain(getMainObb(packageName));
    setObbPatch(getPatchObb(packageName));
    setStatus(STATUS_COMPLETED);
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

  public boolean isSystemApp() {
    return isSystemApp;
  }

  public void setIsSystemApp(boolean systemApp) {
    this.isSystemApp = systemApp;
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
    return obbMain;
  }

  public void setObbMain(Obb obbMain) {
    this.obbMain = obbMain;
  }

  public Obb getObbPatch() {
    return obbPatch;
  }

  public void setObbPatch(Obb obbPatch) {
    this.obbPatch = obbPatch;
  }

  public boolean isUploaded() {
    return isUploaded;
  }

  public void setIsUploaded(boolean uploaded) {
    isUploaded = uploaded;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public ApplicationInfo getAppInfo() {
    return appInfo;
  }

  public void setAppInfo(ApplicationInfo appInfo) {
    this.appInfo = appInfo;
  }

  private Obb getMainObb(String packageName) {
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

  private Obb getPatchObb(String packageName) {
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
    List<FileToUpload> splits = getSplits();
    for (FileToUpload split : splits) {
      if (split.getPath()
          .contains("split_config")) {
        list.add(split);
      }
    }
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
