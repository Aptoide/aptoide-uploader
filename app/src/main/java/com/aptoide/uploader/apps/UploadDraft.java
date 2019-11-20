package com.aptoide.uploader.apps;

public class UploadDraft {

  private int draftId;
  private Status status;
  private InstalledApp installedApp;
  private String md5;

  public UploadDraft(InstalledApp installedApp, String md5) {
    this.installedApp = installedApp;
    this.md5 = md5;
  }

  public UploadDraft(Status status, InstalledApp installedApp, String md5) {
    this.status = status;
    this.installedApp = installedApp;
    this.md5 = md5;
  }

  public UploadDraft(Status status, InstalledApp installedApp, String md5, int draftId) {
    this.status = status;
    this.installedApp = installedApp;
    this.md5 = md5;
    this.draftId = draftId;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public int getDraftId() {
    return draftId;
  }

  public void setDraftId(int draftId) {
    this.draftId = draftId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public InstalledApp getInstalledApp() {
    return installedApp;
  }

  public void setInstalledApp(InstalledApp installedApp) {
    this.installedApp = installedApp;
  }

  public enum Status {
    START, PENDING, STATUS_SET, PROGRESS, COMPLETED, CLIENT_ERROR, NOT_EXISTENT, NO_META_DATA, DUPLICATE, OBB_MAIN, OBB_PATCH, META_DATA_ADDED, RETRY, INTELLECTUAL_RIGHTS, INFECTED, INVALID_SIGNATURE, PUBLISHER_ONLY, APP_BUNDLE, FAILED
  }
}