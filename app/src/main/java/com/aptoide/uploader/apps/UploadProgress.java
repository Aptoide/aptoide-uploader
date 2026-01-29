package com.aptoide.uploader.apps;

public class UploadProgress {

  private final int progress;
  private final String packageName;
  private final String filename;

  public UploadProgress(int progress, String packageName, String filename) {
    this.progress = progress;
    this.packageName = packageName;
    this.filename = filename;
  }

  public int getProgress() {
    return progress;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getFilename() {
    return filename;
  }
}
