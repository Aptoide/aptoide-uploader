package com.aptoide.uploader.account.view;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;

public class MaintenanceNavigator {

  private final static String BLOG_URL = "https://blog.aptoide.com";
  private final Context applicationContext;
  private final FragmentManager fragmentManager;

  public MaintenanceNavigator(Context applicationContext, FragmentManager fragmentManager) {
    this.applicationContext = applicationContext;
    this.fragmentManager = fragmentManager;
  }

  public void openBlogUrl() {
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.addDefaultShareMenuItem();
    builder.setToolbarColor(applicationContext.getResources()
        .getColor(R.color.blue));
    builder.build()
        .launchUrl(applicationContext, Uri.parse(BLOG_URL));
  }

  public void navigateToLoginFragment() {
    navigateTo(LoginFragment.newInstance());
  }

  private void navigateTo(Fragment fragment) {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, fragment)
        .commitAllowingStateLoss();
  }
}
