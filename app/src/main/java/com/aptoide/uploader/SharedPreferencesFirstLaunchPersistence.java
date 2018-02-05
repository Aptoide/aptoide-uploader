package com.aptoide.uploader;

import android.content.SharedPreferences;

/**
 * Created by filipe on 02-02-2018.
 */

public class SharedPreferencesFirstLaunchPersistence implements FirstLaunchPersistence {

  private static final String IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH";
  private final SharedPreferences preferences;

  public SharedPreferencesFirstLaunchPersistence(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  @Override public boolean isFirstLaunch() {
    if (preferences.getBoolean(IS_FIRST_LAUNCH, true)) {
      setFirstLaunch(false);
      return true;
    }
    return false;
  }

  public void setFirstLaunch(boolean isFirstLaunch) {
    preferences.edit()
        .putBoolean(IS_FIRST_LAUNCH, isFirstLaunch)
        .apply();
  }
}
