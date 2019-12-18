package com.aptoide.uploader.apps.network;

import java.util.List;

public class SetDraftSplitMd5sRequest {

  final List<String> split_md5sums;
  final Integer draft_id;
  final String access_token;
  private String apk_md5sum;

  public SetDraftSplitMd5sRequest(String access_token, Integer draft_id, List<String> split_md5sums,
      String md5) {
    this.split_md5sums = split_md5sums;
    this.draft_id = draft_id;
    this.access_token = access_token;
    this.apk_md5sum = md5;
  }
}