package com.aptoide.uploader.security;

import io.reactivex.Single;

public interface AuthenticationProvider {

  Single<String> getAccessToken(String username, String password);

  Single<String> getAccessTokenOAuth(String email, String token, String authMode);

  Single<String> getAccessToken();

  Single<String> getNewAccessToken();

  Single<String> getRefreshToken();

  void saveAuthentication(String accessToken, String refreshToken);
}
