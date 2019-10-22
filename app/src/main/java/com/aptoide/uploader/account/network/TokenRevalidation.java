package com.aptoide.uploader.account.network;

import com.squareup.moshi.Json;

public class TokenRevalidation {

  @Json(name = "access_token") private String accessToken;
  @Json(name = "error_description") private String errorDescription;

  public TokenRevalidation(String accessToken, String errorDescription, String error) {
    this.accessToken = accessToken;
    this.errorDescription = errorDescription;
    this.error = error;
  }

  private String error;

  public String getAccessToken() {
    return accessToken;
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
}
