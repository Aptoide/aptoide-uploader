package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.App;
import java.util.List;

public class ApksResponse {

  private final List<App> list;
  private final Status status;
  private final Errors errors;

  public List<App> getList() {
    return list;
  }

  public Status getStatus() {
    return status;
  }

  public Errors getErrors() {
    return errors;
  }

  public ApksResponse(List<App> list, Status status) {

    this.list = list;
    this.status = status;
    this.errors = null;
  }

  public ApksResponse(Errors errors, Status status) {

    this.list = null;
    this.status = status;
    this.errors = errors;
  }

  public enum Status {
    OK, QUEUED, FAIL
  }

  public static class Errors {

    private final String code;
    private final String description;

    public Errors(String code, String description) {
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
}
