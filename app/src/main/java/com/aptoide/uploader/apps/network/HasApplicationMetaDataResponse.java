package com.aptoide.uploader.apps.network;

import com.squareup.moshi.Json;

/**
 * Created by filipegoncalves on 5/11/18.
 */

public class HasApplicationMetaDataResponse {

  private String status;
  @Json(name = "has") private boolean hasMetaData;

  public HasApplicationMetaDataResponse() {
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean hasMetaData() {
    return hasMetaData;
  }

  public void setHasMetaData(boolean hasMetaData) {
    this.hasMetaData = hasMetaData;
  }
}
