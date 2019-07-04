package com.aptoide.uploader.apps.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.view.LoginFragment;
import com.aptoide.uploader.apps.view.MyStoreFragment;

class AppFormNavigator {

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
