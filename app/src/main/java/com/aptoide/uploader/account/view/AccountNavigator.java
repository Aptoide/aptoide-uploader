package com.aptoide.uploader.account.view;

import android.content.Context;
import android.widget.Toast;

public class AccountNavigator {

  private final Context context;

  public AccountNavigator(Context context) {
    this.context = context;
  }

  public void navigateToAppsView() {
    Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show();
  }
}
