package com.aptoide.uploader.apps.view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.Navigator;
import com.aptoide.uploader.account.view.AutoLoginFragment;
import com.aptoide.uploader.account.view.LoginFragment;

/**
 * Created by jose_messejana on 28-12-2017.
 */

class MyStoreNavigator extends Navigator {

  private final FragmentManager fragmentManager;

  public MyStoreNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void navigateToLoginFragment() {
    navigateToWithoutBackSave(LoginFragment.newInstance());
  }

  public void navigateToSettingsFragment() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, SettingsFragment.newInstance())
        .addToBackStack(String.valueOf(R.layout.fragment_my_apps))
        .commit();
  }

  public void navigateToAutoLoginFragment(String name, String avatarPath) {
    Fragment fragment = AutoLoginFragment.newInstance(name, avatarPath);
    navigateToWithoutBackSave(fragment);
  }

  public void navigateToAutoLoginFragment(String name) {
    Fragment fragment = AutoLoginFragment.newInstance(name, null);
    navigateToWithoutBackSave(fragment);
  }

  private void navigateToWithoutBackSave(Fragment fragment) {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, fragment)
        .commit();
  }
}
