package com.aptoide.uploader;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.aptoide.uploader.account.view.MaintenanceFragment;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.permission.PermissionProviderActivity;
import com.aptoide.uploader.apps.view.AppFormFragment;
import com.aptoide.uploader.apps.view.OnBackPressedInterface;

public class MainActivity extends PermissionProviderActivity {

  private MainActivityNavigator mainActivityNavigator;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainActivityNavigator = new MainActivityNavigator(getSupportFragmentManager());

    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.activity_main_container, MaintenanceFragment.newInstance())
          .commit();
    }
  }

  public void parseIntent(Intent intent) {
    if (intent.getAction()
        .equals("navigateToSubmitAppFragment")) {
      String md5 = intent.getStringExtra("md5");
      String appName = intent.getStringExtra("appName");
      mainActivityNavigator.navigateToSubmitAppView(md5, appName);
    }
    if (intent.getAction()
        .equals("dismissNotification")) {
      String md5 = intent.getStringExtra("md5");
      UploadManager uploadManager =
          ((UploaderApplication) getApplicationContext()).getUploadManager();
      uploadManager.removeUploadFromPersistence(md5)
          .subscribe();
      uploadManager.removeUploadFromQueue(md5);
    }
  }

  @Override public void onBackPressed() {

    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_container);

    if (!(fragment instanceof AppFormFragment)) {
      super.onBackPressed();
    } else {
      ((OnBackPressedInterface) fragment).onBackPressed();
    }
  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override protected void onNewIntent(Intent intent) {
    parseIntent(intent);
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
