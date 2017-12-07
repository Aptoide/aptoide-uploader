package com.aptoide.uploader.apps;

public class Upload {

  private final boolean uploaded;
  private final boolean proposedData;

  public Upload(boolean uploaded, boolean proposedData) {
    this.uploaded = uploaded;
    this.proposedData = proposedData;
  }

  public boolean isUploaded() {
    return uploaded;
  }

  public boolean hasProposedData() {
    return proposedData;
  }

  public enum Status {
    PENDING, COMPLETED
  }
}
