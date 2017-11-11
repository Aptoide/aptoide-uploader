package com.aptoide.uploader.account.view;

import android.content.Context;
import android.widget.Toast;

public class AccountNavigator {

  private final Context context;

  public AccountNavigator(Context context) {
    this.context = context;
  }

  public void navigateToMyAppsView() {
    Toast.makeText(context, "my apps view not implemented", Toast.LENGTH_SHORT).show();
  }

  public void navigateToCreateStoreView() {
    Toast.makeText(context, "create store view not implemented", Toast.LENGTH_SHORT).show();
  }
}
