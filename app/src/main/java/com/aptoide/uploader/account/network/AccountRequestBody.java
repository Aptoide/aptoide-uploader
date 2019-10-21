package com.aptoide.uploader.account.network;

import com.squareup.moshi.Json;
import java.util.List;

/**
 * Created by franciscoaleixo on 03/11/2017.
 */

public class AccountRequestBody {
  @Json(name = "nodes") private List<String> nodes;
  @Json(name = "access_token") private String accessToken;

  public AccountRequestBody(List<String> nodes, String accessToken) {
    this.nodes = nodes;
    this.accessToken = accessToken;
  }
}
