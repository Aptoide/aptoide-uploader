package com.aptoide.uploader.apps.notifications;

public class UploadNotification {
  private final String appName;
  private final String packageName;
  private final String md5;
  private final Type type;
  private final int progress;

  public UploadNotification(String appName, String packageName, String md5, Type type) {
    this.appName = appName;
    this.packageName = packageName;
    this.md5 = md5;
    this.type = type;
    this.progress = -1;
  }

  public UploadNotification(String appName, String packageName, String md5, Type type,
      int progress) {
    this.appName = appName;
    this.packageName = packageName;
    this.md5 = md5;
    this.type = type;
    this.progress = progress;
  }

  public String getAppName() {
    return appName;
  }

  public String getPackageName() {
    return packageName;
  }

  public Type getType() {
    return type;
  }

  public String getMd5() {
    return md5;
  }

  @Override public int hashCode() {
    int result = appName != null ? appName.hashCode() : 0;
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (md5 != null ? md5.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + progress;
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UploadNotification that = (UploadNotification) o;

    if (progress != that.progress) return false;
    if (appName != null ? !appName.equals(that.appName) : that.appName != null) return false;
    if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) {
      return false;
    }
    if (md5 != null ? !md5.equals(that.md5) : that.md5 != null) return false;
    return type == that.type;
  }

  @Override public String toString() {
    return "UploadNotification{" + "appName='" + appName + '\'' + ", type=" + type + '}';
  }

  public int getProgress() {
    return progress;
  }

  public enum Type {
    MORE_INFO_NEEDED, COMPLETED, ALREADY_IN_STORE, CLIENT_TIMEOUT, INFECTED, PUBLISHER_ONLY, INVALID_SIGNATURE, CANNOT_DISTRIBUTE, CATAPPULT_CERTIFIED, APP_BUNDLE_NOT_SUPPORTED, ANTI_SPAM, TRY_AGAIN, FAILED, ERROR_TRY_AGAIN, UNKNOWN_ERROR, HIDDEN, PROGRESS, INDETERMINATE
  }
}
