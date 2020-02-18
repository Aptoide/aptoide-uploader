package com.aptoide.uploader.apps.view;

import android.content.Context;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;

public class AppFormNavigator {

  private final FragmentManager fragmentManager;
  private final Context context;

  public AppFormNavigator(FragmentManager fragmentManager, Context context) {
    this.fragmentManager = fragmentManager;
    this.context = context;
  }

  public void navigateToMyAppsView() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, MyStoreFragment.newInstance())
        .commit();
  }
}
