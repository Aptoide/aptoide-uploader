package com.aptoide.uploader.apps;

public class UploadProgress {

  private final int progress;
  private final String packageName;

  public UploadProgress(int progress, String packageName) {
    this.progress = progress;
    this.packageName = packageName;
  }

  public int getProgress() {
    return progress;
  }

  public String getPackageName() {
    return packageName;
  }
}
