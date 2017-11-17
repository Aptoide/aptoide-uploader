package com.aptoide.uploader.account.view;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.view.MyAppsFragment;

public class AccountNavigator {

  private final Context context;
  private final FragmentManager fragmentManager;

  public AccountNavigator(Context context, FragmentManager fragmentManager) {
    this.context = context;
    this.fragmentManager = fragmentManager;
  }

  public void navigateToMyAppsView() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, MyAppsFragment.newInstance())
        .commit();
  }

  public void navigateToCreateStoreView() {
    Toast.makeText(context, "create store view not implemented", Toast.LENGTH_SHORT)
        .show();
  }
}
