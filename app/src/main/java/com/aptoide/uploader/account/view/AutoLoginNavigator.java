package com.aptoide.uploader.account.view;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.view.MyStoreFragment;

public class AutoLoginNavigator {

  private final FragmentManager fragmentManager;
  private final Context applicationContext;

  public AutoLoginNavigator(FragmentManager fragmentManager, Context applicationContext) {
    this.fragmentManager = fragmentManager;
    this.applicationContext = applicationContext;
  }

  public void navigateToOtherLogins() {
    navigateTo(LoginFragment.newInstance());
  }

  public void navigateToMyAppsView() {
    navigateTo(MyStoreFragment.newInstance());
  }

  private void navigateTo(Fragment fragment) {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, fragment)
        .addToBackStack(String.valueOf(R.layout.fragment_autologin))
        .commitAllowingStateLoss();
  }
}
