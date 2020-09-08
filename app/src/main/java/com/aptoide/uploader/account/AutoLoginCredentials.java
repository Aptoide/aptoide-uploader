package com.aptoide.uploader.account;

public class AutoLoginCredentials {

  private String accessToken;
  private String refreshToken;
  private String storeName;
  private String email;
  private String name;
  private String avatarPath;

  public AutoLoginCredentials() {
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setStoreName(String storeName) {
    this.storeName = storeName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAvatarPath(String avatarPath) {
    this.avatarPath = avatarPath;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getStoreName() {
    return storeName;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getAvatarPath() {
    return avatarPath;
  }
}
