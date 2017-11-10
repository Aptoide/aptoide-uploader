package com.aptoide.uploader;

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
