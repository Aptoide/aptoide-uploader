package com.aptoide.uploader.apps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "installed") public class RoomInstalled {

  @Ignore public static final String ICON = "icon";
  @Ignore public static final String PACKAGE_NAME = "packageName";
  @Ignore public static final String NAME = "name";
  @Ignore public static final String VERSION_CODE = "versionCode";
  @Ignore public static final String VERSION_NAME = "versionName";
  @Ignore public static final String SIGNATURE = "signature";
  @Ignore public static final String STORE_NAME = "storeName";
  @Ignore public static final int STATUS_UNINSTALLED = 1;
  @Ignore public static final int STATUS_WAITING = 2;
  @Ignore public static final int STATUS_INSTALLING = 3;
  @Ignore public static final int STATUS_COMPLETED = 4;
  @Ignore public static final int STATUS_ROOT_TIMEOUT = 5;
  @Ignore public static final int TYPE_DEFAULT = 0;
  @Ignore public static final int TYPE_ROOT = 1;
  @Ignore public static final int TYPE_SYSTEM = 2;
  @Ignore public static final int TYPE_PACKAGE_INSTALLER = 3;
  @Ignore public static final int TYPE_UNKNOWN = -1;

  @PrimaryKey @NonNull private String packageAndVersionCode;
  private String icon;
  private String packageName;
  private String name;
  private int versionCode;
  private String versionName;
  private String signature;
  private boolean systemApp;
  private String storeName;
  private int status;
  private int type;

  public RoomInstalled() {
  }

  public RoomInstalled(@NonNull PackageInfo packageInfo, PackageManager packageManager) {
    this(packageInfo, null, packageManager);
  }

  public RoomInstalled(@NonNull PackageInfo packageInfo, @Nullable String storeName,
      PackageManager packageManager) {
    setIcon(getApkIconPath(packageInfo));
    setName(getApkLabel(packageInfo, packageManager));
    setPackageName(packageInfo.packageName);
    setVersionCode(packageInfo.versionCode);
    setVersionName(packageInfo.versionName);
    setStoreName(storeName);
    this.packageAndVersionCode = packageInfo.packageName + packageInfo.versionCode;
    setSystemApp((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
      setSignature(packageInfo.signatures[0].toString());
    }
    setStatus(STATUS_COMPLETED);
    setType(TYPE_UNKNOWN);
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
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

  public int getVersionCode() {
    return versionCode;
  }

  public void setVersionCode(int versionCode) {
    this.versionCode = versionCode;
  }

  public String getVersionName() {
    return versionName;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public boolean isSystemApp() {
    return systemApp;
  }

  public void setSystemApp(boolean systemApp) {
    this.systemApp = systemApp;
  }

  public String getStoreName() {
    return storeName;
  }

  public void setStoreName(String storeName) {
    this.storeName = storeName;
  }

  public String getPackageAndVersionCode() {
    return packageAndVersionCode;
  }

  public void setPackageAndVersionCode(String packageAndVersionCode) {
    this.packageAndVersionCode = packageAndVersionCode;
  }

  public static String getApkLabel(PackageInfo packageInfo, PackageManager packageManager) {
    return packageInfo.applicationInfo.loadLabel(packageManager)
        .toString();
  }

  public static String getApkIconPath(PackageInfo packageInfo) {
    return "android.resource://"
        + packageInfo.packageName
        + "/"
        + packageInfo.applicationInfo.icon;
  }
}
