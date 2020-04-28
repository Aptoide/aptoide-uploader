package com.aptoide.uploader.account;

import android.content.SharedPreferences;

public class MaintenancePersistence {

  private final static String MAINTENANCE_LOGOUT = "MAINTENANCE_LOGOUT";
  private final SharedPreferences preferences;

  public MaintenancePersistence(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  public boolean shouldLogout() {
    return !preferences.getBoolean(MAINTENANCE_LOGOUT, false);
  }

  public void saveLogout() {
    preferences.edit()
        .putBoolean(MAINTENANCE_LOGOUT, true)
        .apply();
  }
}
