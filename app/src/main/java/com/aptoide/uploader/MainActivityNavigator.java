package com.aptoide.uploader;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.aptoide.uploader.apps.view.AppFormFragment;

class MainActivityNavigator {

  private final FragmentManager fragmentManager;

  public MainActivityNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void navigateToSubmitAppView(String md5) {
    navigateToWithoutBackSave(R.id.activity_main_container, AppFormFragment.newInstance(md5), true);
  }

  private void navigateToWithoutBackSave(int containerId, Fragment fragment, boolean replace) {
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    if (replace) {
      fragmentTransaction = fragmentTransaction.replace(containerId, fragment);
    } else {
      fragmentTransaction = fragmentTransaction.add(containerId, fragment);
    }
    fragmentTransaction.commit();
  }
}