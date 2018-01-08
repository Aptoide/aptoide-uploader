package com.aptoide.uploader.apps.network;

/**
 * Created by filipe on 26-12-2017.
 */

public class GetProposedRequestBody {

  private String language_code;
  private String package_name;
  private boolean filter;

  public GetProposedRequestBody(String language_code, String package_name, boolean filter) {
    this.language_code = language_code;
    this.package_name = package_name;
    this.filter = filter;
  }
}
