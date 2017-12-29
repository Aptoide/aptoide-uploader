package com.aptoide.uploader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.aptoide.uploader.account.view.LoginFragment;
import com.aptoide.uploader.apps.permission.PermissionServiceActivity;

public class MainActivity extends PermissionServiceActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.activity_main_container, LoginFragment.newInstance())
          .commit();
    }
  }
}
