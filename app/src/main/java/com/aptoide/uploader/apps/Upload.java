package com.aptoide.uploader.apps;

public class Upload {

  private final boolean uploaded;
  private final boolean proposedData;
  private final InstalledApp installedApp;
  private final Status status;

  public Upload(boolean uploaded, boolean proposedData, InstalledApp installedApp, Status status) {
    this.uploaded = uploaded;
    this.proposedData = proposedData;
    this.installedApp = installedApp;
    this.status = status;
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

  public enum Status {
    PENDING, PROGRESS, COMPLETED
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Upload upload = (Upload) o;

    return installedApp.equals(upload.installedApp);
  }

  @Override public int hashCode() {
    return installedApp.hashCode();
  }
}
