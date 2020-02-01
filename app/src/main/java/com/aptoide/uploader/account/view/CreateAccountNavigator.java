package com.aptoide.uploader.account.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.view.MyStoreFragment;

class CreateAccountNavigator {

  private static final String RECOVER_PASS = "https://www.aptoide.com/account/password-recovery";

  private final FragmentManager fragmentManager;
  private final Context context;

  public CreateAccountNavigator(FragmentManager fragmentManager, Context context) {
    this.fragmentManager = fragmentManager;
    this.context = context;
  }

  public void navigateToMyAppsView() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, MyStoreFragment.newInstance())
        .commit();
  }

  public void navigateToLoginView() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, LoginFragment.newInstance())
        .commit();
  }

  public void navigateToRecoverPassView() {
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RECOVER_PASS)));
  }
}
