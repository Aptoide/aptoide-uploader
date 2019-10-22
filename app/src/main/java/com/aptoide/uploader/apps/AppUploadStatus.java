package com.aptoide.uploader.apps;

public class AppUploadStatus {

  private final String md5;
  private final String packageName;
  private boolean uploaded;
  private final String vercode;

  public AppUploadStatus(String md5, String packageName, boolean uploaded, String vercode) {
    this.md5 = md5;
    this.packageName = packageName;
    this.uploaded = uploaded;
    this.vercode = vercode;
  }

  public String getMd5() {
    return md5;
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean isUploaded() {
    return uploaded;
  }

  public String getVercode() {
    return vercode;
  }

  public void setUploaded(boolean isUploadedStatus) {
    uploaded = isUploadedStatus;
  }
}
