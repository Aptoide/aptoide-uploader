package com.aptoide.uploader.account;

public class AutoLoginCredentials {

  private String accessToken;
  private String refreshToken;

  public AutoLoginCredentials() {
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }
}
