package com.aptoide.uploader.account.view;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import com.aptoide.uploader.R;

public class MaintenanceNavigator {

  private final static String BLOG_URL = "https://blog.aptoide.com";
  private final Context applicationContext;

  public MaintenanceNavigator(Context applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void openBlogUrl() {
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.addDefaultShareMenuItem();
    builder.setToolbarColor(applicationContext.getResources()
        .getColor(R.color.blue));
    builder.build()
        .launchUrl(applicationContext, Uri.parse(BLOG_URL));
  }
}
