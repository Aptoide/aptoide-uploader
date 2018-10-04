/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import com.facebook.login.LoginManager;
import pt.caixamagica.aptoide.uploader.AptoideUploaderApplication;
import pt.caixamagica.aptoide.uploader.FragmentAppView;
import pt.caixamagica.aptoide.uploader.LoginFragment;
import pt.caixamagica.aptoide.uploader.R;
import pt.caixamagica.aptoide.uploader.dialog.ConfirmationDialog;

/**
 * Created by neuro on 17-04-2015.
 */
public class AppsListActivity extends ActionBarActivity
    implements ConfirmationDialog.ConfirmCallback {

  Bundle bundle;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));

    this.bundle = getIntent().getExtras();
    if (savedInstanceState == null) {
      switchtoSignUpFragment(bundle);
    }
  }

  private void switchtoSignUpFragment(Bundle bundle) {
    Fragment appViewFragment = new FragmentAppView();
    appViewFragment.setArguments(bundle);

    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, appViewFragment, "home")
        .commit();
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    ((AptoideUploaderApplication) getApplicationContext()).getAppsInStoreController()
        .stop();
    removeUserCredentials();
    clearSessionInformation();
    clearAppsInStore();
    AptoideUploaderApplication.setForcedLogout(true);
    switchToLoginFragment();
  }

  private void clearAppsInStore() {
    SharedPreferences.Editor editor =
        getSharedPreferences(AptoideUploaderApplication.APPS_IN_MY_STORE_SHARED_PREFERENCES_FILE,
            MODE_PRIVATE).edit();
    editor.clear();
    editor.apply();
  }

  public void removeUserCredentials() {
    SharedPreferences preferences = getSharedPreferences(LoginFragment.SHARED_PREFERENCES_FILE, 0);
    preferences.edit()
        .remove("token")
        .remove("refreshToken")
        .remove("repo")
        .remove("username")
        .commit();
  }

  private void clearSessionInformation() {
    //Stop the activity
    AptoideUploaderApplication.setForcedLogout(true);
    LoginManager.getInstance()
        .logOut();
  }

  public void switchToLoginFragment() {
    Intent intent = new Intent(this, LoginActivity.class);
    this.startActivity(intent);
    this.finish();
  }
}
