package com.aptoide.uploader.apps.view;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
    navigateToWithoutBackSave(R.id.activity_main_container, LoginFragment.newInstance(), true);
  }

  public void navigateToAutoLoginFragment(String name, String avatarPath) {
    Fragment fragment = AutoLoginFragment.newInstance(name, avatarPath);
    navigateToWithoutBackSave(R.id.activity_main_container, fragment, true);
  }

  public void navigateToAutoLoginFragment(String name) {
    Fragment fragment = AutoLoginFragment.newInstance(name, null);
    navigateToWithoutBackSave(R.id.activity_main_container, fragment, true);
  }

  public void navigateToMyStoreFragment() {
    navigateToWithoutBackSave(R.id.activity_main_container, MyStoreFragment.newInstance(), true);
  }

  public void navigateToAutoUploadFragment() {
    navigateToWithoutBackSave(R.id.activity_main_container, AutoUploadFragment.newInstance(), false);
  }

  public void navigateToAutoUploadFragment1() {
    fragmentManager.beginTransaction()
        .replace(R.id.activity_main_container, AutoUploadFragment.newInstance())
        .addToBackStack(String.valueOf(R.layout.fragment_settings))
        .commit();
  }

  private void navigateToWithoutBackSave(int containerId, Fragment fragment, boolean replace) {
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    if (replace) {
      fragmentTransaction = fragmentTransaction.replace(containerId, fragment);
    } else {
      fragmentTransaction = fragmentTransaction.add(containerId, fragment);
    }

    fragmentTransaction.commit();
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