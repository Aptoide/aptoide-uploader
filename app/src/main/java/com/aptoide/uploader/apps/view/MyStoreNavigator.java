package com.aptoide.uploader.apps.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.view.LoginFragment;

/**
 * Created by jose_messejana on 28-12-2017.
 */

class MyStoreNavigator {

  private final FragmentManager fragmentManager;

  public MyStoreNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void navigateToLoginView() {
    navigateToWithoutBackSave(R.id.activity_main_container, LoginFragment.newInstance(), true);
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
