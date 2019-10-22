package com.aptoide.uploader.account.network;

import java.util.List;

public class CreateStoreResponse {

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
