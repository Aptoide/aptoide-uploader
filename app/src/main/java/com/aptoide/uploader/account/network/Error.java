package com.aptoide.uploader.account.network;

import java.util.List;

public class Error {
  private String code;
  private String description;
  private List<String> details;

  public Error(String code, String description, List<String> details) {
    this.code = code;
    this.description = description;
    this.details = details;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getDetails() {
    return details;
  }
}
