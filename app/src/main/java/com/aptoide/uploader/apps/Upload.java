package com.aptoide.uploader.apps;

public class Upload {

  private final boolean uploaded;
  private final boolean proposedData;
  private final InstalledApp installedApp;
  private final Status status;
  private final String md5;
  private final String storeName;

  public Upload(boolean uploaded, boolean proposedData, InstalledApp installedApp, Status status,
      String md5, String storeName) {
    this.uploaded = uploaded;
    this.proposedData = proposedData;
    this.installedApp = installedApp;
    this.status = status;
    this.md5 = md5;
    this.storeName = storeName;
  }

  public String getStoreName() {
    return storeName;
  }

  public boolean isUploaded() {
    return uploaded;
  }

  public boolean hasProposedData() {
    return proposedData;
  }

  public InstalledApp getInstalledApp() {
    return installedApp;
  }

  public Status getStatus() {
    return status;
  }

  @Override public int hashCode() {
    return installedApp.hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Upload upload = (Upload) o;

    return installedApp.equals(upload.installedApp);
  }

  public String getMd5() {
    return md5;
  }

  public enum Status {
    PENDING, PROGRESS, COMPLETED, CLIENT_ERROR, DUPLICATE
  }
}
