package com.aptoide.uploader.apps;

import java.util.List;

public class UploadDraft {

  private int draftId;
  private Status status;
  private InstalledApp installedApp;
  private String md5;
  private List<String> splitsToBeUploaded;
  private Metadata metadata;

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
    START, STATUS_SET_PENDING, STATUS_SET_DRAFT, SET_STATUS_TO_DRAFT, PROGRESS, COMPLETED, CLIENT_ERROR, NOT_EXISTENT, NO_META_DATA, DUPLICATE, META_DATA_ADDED, INTELLECTUAL_RIGHTS, INFECTED, INVALID_SIGNATURE, PUBLISHER_ONLY, APP_BUNDLE, UPLOAD_FAILED, WAITING_UPLOAD_CONFIRMATION, UPLOAD_PENDING, DRAFT_CREATED, MD5S_SET, METADATA_SET, UPLOAD_FAILED_RETRY, UNKNOWN_ERROR_RETRY, UNKNOWN_ERROR
  }

  public void setSplitsToBeUploaded(List<String> md5s) {
    splitsToBeUploaded = md5s;
  }

  public List<String> getSplitsToBeUploaded() {
    return splitsToBeUploaded;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }
}