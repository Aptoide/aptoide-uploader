package com.aptoide.uploader.account.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.view.android.FragmentView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class AutoLoginFragment extends FragmentView implements AutoLoginView {
  private Button autoLoginButton;
  private Button otherLoginsButton;
  private ImageView autoLoginAvatar;
  private AptoideAccountManager accountManager;

  public AutoLoginFragment() {
  }

  public static AutoLoginFragment newInstance(String loginName, String loginAvatarPath) {
    AutoLoginFragment autoLoginFragment = new AutoLoginFragment();
    Bundle bundle = new Bundle();
    bundle.putString("loginName", loginName);
    bundle.putString("loginAvatarPath", loginAvatarPath);
    autoLoginFragment.setArguments(bundle);
    return autoLoginFragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    accountManager =
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    autoLoginButton = view.findViewById(R.id.autologin_aptoide);
    autoLoginAvatar = view.findViewById(R.id.autologin_avatar);

    otherLoginsButton = view.findViewById(R.id.login_other_logins);

    new AutoLoginPresenter(this, accountManager,
        ((UploaderApplication) getContext().getApplicationContext()).getUploaderAnalytics(),
        ((UploaderApplication) getContext().getApplicationContext()).getAutoLoginManager(),
        new AutoLoginNavigator(getFragmentManager(), getContext().getApplicationContext()),
        new CompositeDisposable(), AndroidSchedulers.mainThread()).present();
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_autologin, container, false);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Override public Observable<Object> clickAutoLogin() {
    return RxView.clicks(autoLoginButton);
  }

  @Override public Observable<Object> clickOtherLogins() {
    return RxView.clicks(otherLoginsButton);
  }

  @Override public void showLoginAvatar() {
    Bundle bundle = this.getArguments();
    if (bundle.getString("loginAvatarPath") != null && !bundle.getString("loginAvatarPath")
        .trim()
        .isEmpty()) {
      Glide.with(this)
          .load(bundle.getString("loginAvatarPath"))
          .apply(RequestOptions.circleCropTransform())
          .into(autoLoginAvatar);
    } else {
      Glide.with(this)
          .load(getResources().getDrawable(R.drawable.avatar_default))
          .apply(RequestOptions.circleCropTransform())
          .into(autoLoginAvatar);
    }
  }

  @Override public void showLoginName() {
    Bundle bundle = this.getArguments();
    autoLoginButton.setText(
        String.format(getString(R.string.login_as_button), bundle.getString("loginName")));
    otherLoginsButton.setText(R.string.login_other_account_button);
  }

  @Override public void showLoginMessage() {
    Bundle bundle = this.getArguments();
    Toast.makeText(getContext(),
        getString(R.string.logging_as) + " " + bundle.getString("loginName"), Toast.LENGTH_LONG)
        .show();
  }

  @Override public void showNetworkError() {
    Toast.makeText(getContext(), getString(R.string.connection_error_body), Toast.LENGTH_LONG)
        .show();
  }
}
