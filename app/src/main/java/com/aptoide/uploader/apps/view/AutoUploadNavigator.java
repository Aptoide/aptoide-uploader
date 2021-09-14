package com.aptoide.uploader.apps.view;

import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;

public class AutoUploadNavigator {
  private final FragmentManager fragmentManager;

  public AutoUploadNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void navigateToSettingsFragment() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, SettingsFragment.newInstance())
        .commit();
  }
}
