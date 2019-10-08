package com.aptoide.uploader.account.network;

import com.squareup.moshi.Json;

/**
 * Created by jdandrade on 03/11/2017.
 */

public class OAuth {

  @Json(name = "access_token") private String accessToken;
  @Json(name = "refresh_token") private String refreshToken;
  @Json(name = "error_description") private String errorDescription;
  @Json(name = "status") private String status;

  public OAuth(String accessToken, String refreshToken, String errorDescription, String error,
      String status) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.errorDescription = errorDescription;
    this.error = error;
    this.status = status;
  }

  private String error;

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getErrorDescription() {
    return errorDescription;
  }

  public String getError() {
    return error;
  }

  public boolean hasErrors() {
    return error != null;
  }

  public String getStatus() {
    return status;
  }
}
