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

  public UploadDraft(Status status, InstalledApp installedApp, String md5, int draftId,
      Metadata metadata) {
    this.status = status;
    this.installedApp = installedApp;
    this.md5 = md5;
    this.draftId = draftId;
    this.metadata = metadata;
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

  public List<String> getSplitsToBeUploaded() {
    return splitsToBeUploaded;
  }

  public void setSplitsToBeUploaded(List<String> md5s) {
    splitsToBeUploaded = md5s;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  @Override public int hashCode() {
    return md5.hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UploadDraft that = (UploadDraft) o;

    return md5.equals(that.md5);
  }

  @Override public String toString() {
    return "UploadDraft{" + draftId + " status=" + status + ", installedApp=" + installedApp + '}';
  }

  public boolean isInProgress() {
    return this.getStatus() == Status.PROGRESS
        || this.getStatus() == Status.STATUS_SET_PENDING
        || this.getStatus() == Status.STATUS_SET_DRAFT
        || this.getStatus() == Status.DRAFT_CREATED
        || this.getStatus() == Status.MD5S_SET
        || this.getStatus() == Status.NOT_EXISTENT
        || this.getStatus() == Status.NO_META_DATA
        || this.getStatus() == Status.META_DATA_ADDED
        || this.getStatus() == Status.WAITING_UPLOAD_CONFIRMATION
        || this.getStatus() == Status.MISSING_SPLITS
        || this.getStatus() == Status.METADATA_SET
        || this.getStatus() == Status.SET_STATUS_TO_DRAFT;
  }

  public boolean isError() {
    return this.getStatus() == Status.CLIENT_ERROR
        || this.getStatus() == Status.UNKNOWN_ERROR
        || this.getStatus() == Status.UNKNOWN_ERROR_RETRY;
  }

  public enum Status {
    IN_QUEUE, STATUS_SET_PENDING, STATUS_SET_DRAFT, SET_STATUS_TO_DRAFT, PROGRESS, COMPLETED, CLIENT_ERROR, NOT_EXISTENT, NO_META_DATA, DUPLICATE, META_DATA_ADDED, INTELLECTUAL_RIGHTS, INFECTED, INVALID_SIGNATURE, PUBLISHER_ONLY, APP_BUNDLE, UPLOAD_FAILED, WAITING_UPLOAD_CONFIRMATION, MISSING_SPLITS, DRAFT_CREATED, MD5S_SET, METADATA_SET, UPLOAD_FAILED_RETRY, UNKNOWN_ERROR_RETRY, UNKNOWN_ERROR, EXCEEDED_GET_RETRIES, ANTI_SPAM_RULE, MISSING_ARGUMENTS, CATAPPULT_CERTIFIED
  }
}