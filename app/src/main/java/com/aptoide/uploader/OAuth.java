package com.aptoide.uploader;

import com.squareup.moshi.Json;

/**
 * Created by jdandrade on 03/11/2017.
 */

public class OAuth {

  @Json(name = "access_token") private String accessToken;
  @Json(name = "refresh_token") private String refreshToken;
  @Json(name = "error_description") private String errorDescription;
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
}
