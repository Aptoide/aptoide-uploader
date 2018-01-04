package com.aptoide.uploader.security;

import io.reactivex.Single;

/**
 * Created by jdandrade on 03/01/2018.
 */

public interface AuthenticationProvider {

  void saveAccessToken(String accessToken);

  void saveRefreshToken(String refreshToken);

  String getRefreshToken();

  Single<String> getAccessToken(String username, String password);
}
