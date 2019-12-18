package com.aptoide.uploader.account.network;

import com.squareup.moshi.Json;
import java.util.List;

public class Error {
  private String code;
  private String description;
  @Json(name = "details") private Details details;

  public Error(String code, String description, Details details) {
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

  public Details getDetails() {
    return details;
  }

  public static class Details {
    @Json(name = "splits") private List<String> splits;

    public List<String> getSplits() {
      return splits;
    }
  }
}
