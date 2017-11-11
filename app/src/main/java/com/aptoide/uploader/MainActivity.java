package com.aptoide.uploader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.aptoide.uploader.account.view.AccountFragment;

public class MainActivity extends AppCompatActivity {

  private Unbinder viewBinder;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    viewBinder = ButterKnife.bind(this);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.activity_main_container, AccountFragment.newInstance())
          .commit();
    }
  }

  @Override protected void onDestroy() {
    viewBinder.unbind();
    super.onDestroy();
  }
}
