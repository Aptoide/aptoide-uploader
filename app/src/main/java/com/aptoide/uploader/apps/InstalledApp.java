package com.aptoide.uploader.apps;

public class InstalledApp {

  private final String icon;
  private final String name;
  private final boolean isSystem;
  private final String packageName;
  private final String apkPath;

  public InstalledApp(String icon, String name, boolean isSystem, String packageName,
      String apkPath) {
    this.icon = icon;
    this.name = name;
    this.isSystem = isSystem;
    this.packageName = packageName;
    this.apkPath = apkPath;
  }

  public String getIcon() {
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
}
