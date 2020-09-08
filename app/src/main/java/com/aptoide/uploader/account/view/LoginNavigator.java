package com.aptoide.uploader.account.view;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.Navigator;
import com.aptoide.uploader.apps.view.MyStoreFragment;

public class LoginNavigator extends Navigator {

  public final static String NEW_AUTHENTICATION_BLOG_URL =
      "https://blog.aptoide.com/aptoide-new-authentication-system-no-user-data-storage/";

  private final FragmentManager fragmentManager;
  private final Context applicationContext;

  public LoginNavigator(FragmentManager fragmentManager, Context applicationContext) {
    this.fragmentManager = fragmentManager;
    this.applicationContext = applicationContext;
  }

  public void navigateToMyAppsView() {
    navigateTo(MyStoreFragment.newInstance());
  }

  public void navigateToCreateStoreView() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, CreateStoreFragment.newInstance())
        .addToBackStack(String.valueOf(R.layout.fragment_create_store))
        .commitAllowingStateLoss();
  }

  public void navigateToLoginFragment() {
    navigateTo(LoginFragment.newInstance());
  }

  public void navigateToAutoLoginFragment(String name, String avatarPath) {
    Fragment fragment = AutoLoginFragment.newInstance(name, avatarPath);
    navigateTo(fragment);
  }

  public void navigateToAutoLoginFragment(String name) {
    Fragment fragment = AutoLoginFragment.newInstance(name, null);
    navigateTo(fragment);
  }

  private void navigateTo(Fragment fragment) {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, fragment)
        .commitAllowingStateLoss();
  }

  public void openNewAuthenticationBlogUrl() {
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.addDefaultShareMenuItem();
    builder.setToolbarColor(applicationContext.getResources()
        .getColor(R.color.blue));
    builder.build()
        .launchUrl(applicationContext, Uri.parse(NEW_AUTHENTICATION_BLOG_URL));
  }
}
