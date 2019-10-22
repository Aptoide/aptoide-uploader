package com.aptoide.uploader.account.network;

import java.util.List;

public class CreateStoreStatus {

  private Status status;
  private List<Error> errors;

  public CreateStoreStatus(Status status, List<Error> errors) {
    this.status = status;
    this.errors = errors;
  }

  public Status getStatus() {
    return status;
  }

  public List<Error> getErrors() {
    return errors;
  }
}
