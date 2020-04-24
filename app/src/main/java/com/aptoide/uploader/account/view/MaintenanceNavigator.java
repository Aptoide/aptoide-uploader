package com.aptoide.uploader.account.view;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;

public class MaintenanceNavigator {

  public MaintenanceNavigator() {
    String url = "example.com";
    customTab(url);
  }

  public void customTab(String url){
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.addDefaultShareMenuItem();
    //builder.build().launchUrl(this, Uri.parse(url));
  }
}
