package com.aptoide.uploader.apps;

import java.util.List;

public class InstalledAppsStatus {
  private final List<InstalledApp> apps;
  private final List<AppUploadStatus> uploadStatuses;
  private final List<AutoUploadSelects> autoUploadSelects;

  InstalledAppsStatus(List<InstalledApp> apps, List<AppUploadStatus> uploadStatuses,
      List<AutoUploadSelects> autoUploadSelects) {
    this.apps = apps;
    this.uploadStatuses = uploadStatuses;
    this.autoUploadSelects = autoUploadSelects;
  }

  public List<InstalledApp> getInstalledApps() {
    return apps;
  }

  public List<AppUploadStatus> getUploadStatuses() {
    return uploadStatuses;
  }

  public List<AutoUploadSelects> getAutoUploadSelects() {
    return autoUploadSelects;
  }
}
