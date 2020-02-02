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
