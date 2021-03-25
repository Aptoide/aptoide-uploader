package com.aptoide.uploader.apps.view;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.Navigator;
import com.aptoide.uploader.account.view.AutoLoginFragment;
import com.aptoide.uploader.account.view.LoginFragment;

class SettingsNavigator extends Navigator {
  public final static String SEND_FEEDBACK_URL =
      "https://aptoide.zendesk.com/hc/en-us/requests/new";
  public final static String ABOUT_US_URL = "https://en.aptoide.com/company/about-us";
  public final static String TERMS_CONDITIONS_URL =
      "https://en.aptoide.com/company/legal?section=terms";
  public final static String PRIVACY_POLICY_URL =
      "https://en.aptoide.com/company/legal?section=privacy";

  private final FragmentManager fragmentManager;
  private final Context applicationContext;

  public SettingsNavigator(FragmentManager fragmentManager, Context applicationContext) {
    this.fragmentManager = fragmentManager;
    this.applicationContext = applicationContext;
  }

  public void navigateToLoginFragment() {
    navigateToWithoutBackSave(LoginFragment.newInstance());
  }

  public void navigateToAutoLoginFragment(String name, String avatarPath) {
    Fragment fragment = AutoLoginFragment.newInstance(name, avatarPath);
    navigateToWithoutBackSave(fragment);
  }

  public void navigateToAutoLoginFragment(String name) {
    Fragment fragment = AutoLoginFragment.newInstance(name, null);
    navigateToWithoutBackSave(fragment);
  }

  public void navigateToMyStoreFragment() {
    navigateToWithoutBackSave(MyStoreFragment.newInstance());
  }

  public void navigateToAutoUploadFragment() {
    fragmentManager.beginTransaction()
        .add(R.id.activity_main_container, AutoUploadFragment.newInstance())
        .addToBackStack(String.valueOf(R.layout.fragment_settings))
        .commit();
  }

  private void navigateToWithoutBackSave(Fragment fragment) {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, fragment)
        .commit();
  }

  public void openUrl(String url) {
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.addDefaultShareMenuItem();
    builder.setToolbarColor(applicationContext.getResources()
        .getColor(R.color.blue));
    builder.build()
        .launchUrl(applicationContext, Uri.parse(url));
  }

  public void openSendFeedbackUrl() {
    openUrl(SEND_FEEDBACK_URL);
  }

  public void openAboutUsUrl() {
    openUrl(ABOUT_US_URL);
  }

  public void openTermsConditionsUrl() {
    openUrl(TERMS_CONDITIONS_URL);
  }

  public void openPrivacyPolicyUrl() {
    openUrl(PRIVACY_POLICY_URL);
  }
}