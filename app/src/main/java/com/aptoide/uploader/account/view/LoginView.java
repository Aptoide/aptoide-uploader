package com.aptoide.uploader.account.view;

import com.aptoide.uploader.view.View;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import io.reactivex.Observable;

public interface LoginView extends View {

  //Observable<CredentialsViewModel> getLoginEvent();

  Observable<Object> getGoogleLoginEvent();

  //Observable<CredentialsViewModel> getOpenCreateAccountView();

  Observable<GoogleSignInAccount> googleLoginSuccessEvent();

  Observable<LoginResult> facebookLoginSuccessEvent();

  void showLoading(String username);

  void showLoadingWithoutUserName();

  void hideLoading();

  void showCrendentialsError();

  void showNetworkError();

  void showNoConnectivityError();

  void hideKeyboard();

  void startGoogleActivity();

  Observable<Object> getFacebookLoginEvent();

  void navigateToFacebookLogin();

  class CredentialsViewModel {

    private final String username;
    private final String password;

    public CredentialsViewModel(String username, String password) {
      this.username = username;
      this.password = password;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }
}
