package com.aptoide.uploader.account.view;

import android.support.annotation.Nullable;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;

public interface CreateAccountView extends View {

  Observable<ViewModel> getCreateAccountEvent();

  void showLoading();

  void hideLoading();

  void showNetworkError();

  void showErrorUserAlreadyExists();

  class ViewModel {
    private final String email;
    private final String password;
    private final String storeName;
    private final String storeUser;
    private final String storePassword;

    public ViewModel(String email, String password, String storeName) {
      this(email, password, storeName, null, null);
    }

    public ViewModel(String email, String password, String storeName, @Nullable String storeUser,
        @Nullable String storePassword) {
      this.email = email;
      this.password = password;
      this.storeName = storeName;
      this.storeUser = storeUser;
      this.storePassword = storePassword;
    }

    public String getEmail() {
      return email;
    }

    public String getPassword() {
      return password;
    }

    public String getStoreName() {
      return storeName;
    }

    public String getStoreUser() {
      return storeUser;
    }

    public String getStorePassword() {
      return storePassword;
    }

    public boolean isPrivateStore() {
      return storePassword != null && !storePassword.isEmpty();
    }
  }
}
