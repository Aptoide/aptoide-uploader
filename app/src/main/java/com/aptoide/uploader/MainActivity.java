package com.aptoide.uploader;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.aptoide.uploader.account.view.LoginFragment;
import com.aptoide.uploader.apps.permission.PermissionProviderActivity;

public class MainActivity extends PermissionProviderActivity {

  private MainActivityNavigator mainActivityNavigator;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainActivityNavigator = new MainActivityNavigator(getSupportFragmentManager());

    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.activity_main_container, LoginFragment.newInstance())
          .commit();
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    parseIntent(intent);
  }

  public void parseIntent(Intent intent) {
    if (intent.getAction()
        .equals("navigateToSubmitAppFragment")) {
      String md5 = intent.getStringExtra("md5");
      mainActivityNavigator.navigateToSubmitAppView(md5);
    }
  }
}
