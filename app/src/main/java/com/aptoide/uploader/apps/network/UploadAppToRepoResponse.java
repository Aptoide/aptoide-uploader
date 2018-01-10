package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.Error;
import com.aptoide.uploader.account.network.Status;
import java.util.List;

public class UploadAppToRepoResponse {

  private Status status;
  private List<Error> errors;

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public List<Error> getErrors() {
    return errors;
  }

  public void setErrors(List<Error> errors) {
    this.errors = errors;
  }
}
