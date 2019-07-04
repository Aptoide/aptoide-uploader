package com.aptoide.uploader.account.view;

import com.aptoide.uploader.view.View;
import io.reactivex.Observable;

public interface LoginView extends View {

  Observable<CredentialsViewModel> getLoginEvent();

  Observable<CredentialsViewModel> getOpenCreateAccountView();

  void showLoading(String username);

  void hideLoading();

  void showCrendentialsError();

  void showNetworkError();

  void hideKeyboard();

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
