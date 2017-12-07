package com.aptoide.uploader.apps;

public class Upload {

  private final boolean uploaded;
  private final boolean proposedData;
  private final InstalledApp installedApp;

  public Upload(boolean uploaded, boolean proposedData, InstalledApp installedApp) {
    this.uploaded = uploaded;
    this.proposedData = proposedData;
    this.installedApp = installedApp;
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

  public enum Status {
    PENDING, COMPLETED
  }
}
