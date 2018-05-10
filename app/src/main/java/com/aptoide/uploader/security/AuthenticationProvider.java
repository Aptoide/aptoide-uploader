package com.aptoide.uploader.security;

import io.reactivex.Single;

public interface AuthenticationProvider {

  Single<String> getAccessToken(String username, String password);

  Single<String> getAccessToken();

  void saveAuthentication(String accessToken, String refreshToken);
}
