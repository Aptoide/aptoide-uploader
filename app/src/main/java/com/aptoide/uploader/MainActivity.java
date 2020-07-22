package com.aptoide.uploader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.aptoide.uploader.account.view.LoginFragment;
import com.aptoide.uploader.apps.permission.PermissionProviderActivity;
import com.aptoide.uploader.apps.view.AppFormFragment;
import com.aptoide.uploader.apps.view.OnBackPressedInterface;
import com.google.android.material.snackbar.Snackbar;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainActivity extends PermissionProviderActivity implements MainView {

  private MainActivityNavigator mainActivityNavigator;

  private ProgressDialog progressDialog;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mainActivityNavigator = new MainActivityNavigator(getSupportFragmentManager());

    setContentView(R.layout.activity_main);
    progressDialog = createGenericPleaseWaitDialog(this, R.style.DialogTheme);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.activity_main_container, LoginFragment.newInstance())
          .commit();
    }
    UploaderApplication app = ((UploaderApplication) getApplicationContext());
    new MainPresenter(this, app.getAccountManager(), app.getAgentPersistence(),
        AndroidSchedulers.mainThread(), app.getUploadManager(), new MainNavigator(this)).present();
  }

  @Override public void onBackPressed() {

    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_container);

    if (!(fragment instanceof AppFormFragment)) {
      super.onBackPressed();
    } else {
      ((OnBackPressedInterface) fragment).onBackPressed();
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void showLoadingView() {
    progressDialog.show();
  }

  @Override public void hideLoadingView() {
    progressDialog.dismiss();
  }

  @Override public void showGenericErrorMessage() {
    Snackbar.make(findViewById(R.id.activity_main_container),
        getString(R.string.all_message_general_error), Snackbar.LENGTH_LONG)
        .show();
  }

  private ProgressDialog createGenericPleaseWaitDialog(Context context, int resourceId) {
    ProgressDialog progressDialog =
        new ProgressDialog(new ContextThemeWrapper(context, resourceId));
    progressDialog.setMessage(context.getString(R.string.please_wait));
    progressDialog.setCancelable(false);
    return progressDialog;
  }
}
