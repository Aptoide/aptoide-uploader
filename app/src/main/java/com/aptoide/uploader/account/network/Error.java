package com.aptoide.uploader.account.network;

public class Error {
  private String code;
  private String description;

  public Error(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
