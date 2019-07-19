package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.Apk;
import java.util.List;

public class ApksResponse {

  private final List<Apk> list;
  private final Status status;

  public List<Apk> getList() {
    return list;
  }

  public Status getStatus() {
    return status;
  }

  public ApksResponse(List<Apk> list, Status status) {

    this.list = list;
    this.status = status;
  }

  public enum Status {
    OK, QUEUED, FAIL
  }
}
