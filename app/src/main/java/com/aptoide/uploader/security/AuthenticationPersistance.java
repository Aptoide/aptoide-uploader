package com.aptoide.uploader.security;

/**
 * Created by jdandrade on 04/01/2018.
 */

public interface AuthenticationPersistance {

  void saveAuthentication(String accessToken, String refreshToken);

  String getRefreshToken();

  String getAccessToken();
}
