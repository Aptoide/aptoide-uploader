package com.aptoide.uploader.apps.view;

import android.content.Context;
import android.util.Log;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;

public class AutoUploadNavigator {
  private final FragmentManager fragmentManager;
  private final Context applicationContext;

  public AutoUploadNavigator(FragmentManager fragmentManager, Context applicationContext) {
    this.fragmentManager = fragmentManager;
    this.applicationContext = applicationContext;
  }

  public void navigateToSettingsFragment() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, SettingsFragment.newInstance())
        .commit();
  }
}
