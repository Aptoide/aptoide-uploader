package com.aptoide.uploader.apps;

public class MetadataUpload extends Upload {

  private Metadata metadata;

  public MetadataUpload(boolean uploaded, InstalledApp installedApp,
      Status status, String md5, String storeName, Metadata metadata) {
    super(uploaded, installedApp, status, md5, storeName);
    this.metadata = metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public Metadata getMetadata() {
    return metadata;
  }
}
