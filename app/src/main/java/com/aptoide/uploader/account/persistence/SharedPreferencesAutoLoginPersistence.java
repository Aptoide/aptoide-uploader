package com.aptoide.uploader.account.persistence;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import com.aptoide.uploader.account.AutoLoginPersistence;

public class SharedPreferencesAutoLoginPersistence implements AutoLoginPersistence {
  private static final String FORCED_LOGOUT = "FORCED_LOGOUT";
  private final SharedPreferences preferences;

  public SharedPreferencesAutoLoginPersistence(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  @SuppressLint("ApplySharedPref") @Override public boolean isForcedLogout() {
    return preferences.getBoolean(FORCED_LOGOUT, false);
  }

  @SuppressLint("ApplySharedPref") @Override public void setForcedLogout(boolean forcedLogout) {
    preferences.edit()
        .putBoolean(FORCED_LOGOUT, forcedLogout)
        .commit();
  }
}
