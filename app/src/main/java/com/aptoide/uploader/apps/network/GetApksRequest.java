package com.aptoide.uploader.apps.network;

import java.util.List;

public class GetApksRequest {

  final List<String> apk_md5sums;
  final String store_name;
  final String access_token;

  public GetApksRequest(List<String> apk_md5sums, String store_name, String access_token) {
    this.apk_md5sums = apk_md5sums;
    this.store_name = store_name;
    this.access_token = access_token;
  }
}
