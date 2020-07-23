package com.aptoide.uploader.apps.view;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.view.AutoLoginFragment;
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

  public void navigateToAutoLoginFragment(String name) {
    Bundle bundle = new Bundle();
    bundle.putString("name", name);

    Fragment fragment = AutoLoginFragment.newInstance();
    fragment.setArguments(bundle);

    navigateToWithoutBackSave(R.id.activity_main_container, fragment, true);
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
