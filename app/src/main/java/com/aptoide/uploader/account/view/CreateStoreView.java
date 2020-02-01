package com.aptoide.uploader.account.view;

import androidx.annotation.Nullable;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;

public interface CreateStoreView extends View {

  Observable<CreateStoreViewModel> getStoreInfo();

  Observable<CreateStoreViewModel> getOpenLoginView();

  Observable<CreateStoreViewModel> getOpenRecoverPasswordView();

  void showLoading();

  void hideLoading();

  void showInvalidFieldError(String messageError);

  void showNetworkError();

  void showErrorUserAlreadyExists();

  void showErrorStoreAlreadyExists();

  void hideKeyboard();

  class CreateStoreViewModel {
    private final String storeName;
    private final String storeUser;
    private final String storePassword;

    public CreateStoreViewModel(String storeName) {
      this(storeName, null, null);
    }

    public CreateStoreViewModel(String storeName, @Nullable String storeUser,
        @Nullable String storePassword) {
      this.storeName = storeName;
      this.storeUser = storeUser;
      this.storePassword = storePassword;
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