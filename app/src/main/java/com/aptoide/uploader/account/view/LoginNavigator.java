package com.aptoide.uploader.account.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.view.MyStoreFragment;

public class LoginNavigator {

  private final FragmentManager fragmentManager;

  public LoginNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void navigateToMyAppsView() {
    navigateTo(MyStoreFragment.newInstance());
  }

  public void navigateToCreateStoreView() {
    navigateTo(CreateStoreFragment.newInstance());
  }

  public void navigateToCreateAccountView() {
    navigateTo(CreateAccountFragment.newInstance());
  }

  private void navigateTo(Fragment fragment) {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, fragment)
        .commitAllowingStateLoss();
  }
}
