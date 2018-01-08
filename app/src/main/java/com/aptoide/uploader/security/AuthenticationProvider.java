package com.aptoide.uploader.security;

import io.reactivex.Single;

/**
 * Created by jdandrade on 03/01/2018.
 */

public interface AuthenticationProvider {

  Single<String> getAccessToken(String username, String password);

  Single<String> getAccessToken();

  Single<String> getRefreshToken();

  void saveAuthentication(String accessToken, String refreshToken);
}
