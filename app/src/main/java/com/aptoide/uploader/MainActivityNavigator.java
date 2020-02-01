package com.aptoide.uploader;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.aptoide.uploader.apps.view.AppFormFragment;
import com.aptoide.uploader.apps.view.MyStoreFragment;

class MainActivityNavigator {

  private final FragmentManager fragmentManager;

  public MainActivityNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void navigateToSubmitAppView(String md5, String appName) {
    navigateToWithoutBackSave(R.id.activity_main_container,
        AppFormFragment.newInstance(md5, appName), true);
  }

  public void navigateToMyAppsFragment() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, MyStoreFragment.newInstance())
        .commitAllowingStateLoss();
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