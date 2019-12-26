package com.aptoide.uploader.apps.network;

import com.squareup.moshi.Json;
import java.util.List;

public class UploadAppToRepoResponse {

  @Json(name = "status") private String status;
  private List<Error> errors;
  @Json(name = "draft_id") private int draftId;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<Error> getErrors() {
    return errors;
  }

  public void setErrors(List<Error> errors) {
    this.errors = errors;
  }

  public int getDraftId() {
    return draftId;
  }

  public void setDraftId(int draftId) {
    this.draftId = draftId;
  }

  public static class Error {
    private String code;
    private String msg;

    public Error(String code, String msg) {
      this.code = code;
      this.msg = msg;
    }

    public String getCode() {
      return code;
    }

    public String getMsg() {
      return msg;
    }
  }
}
