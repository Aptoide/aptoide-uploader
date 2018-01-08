package com.aptoide.uploader.security;

import android.content.SharedPreferences;

/**
 * Created by jdandrade on 04/01/2018.
 */

public class SharedPreferencesAuthenticationPersistence implements AuthenticationPersistance {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String REFRESH_TOKEN = "refresh_token";
  private final SharedPreferences preferences;

  public SharedPreferencesAuthenticationPersistence(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  @Override public void saveAuthentication(String accessToken, String refreshToken) {
    preferences.edit()
        .putString(ACCESS_TOKEN, accessToken)
        .putString(REFRESH_TOKEN, refreshToken)
        .apply();
  }

  @Override public String getRefreshToken() {
    return preferences.getString(REFRESH_TOKEN, null);
  }

  @Override public String getAccessToken() {
    return preferences.getString(ACCESS_TOKEN, null);
  }
}
