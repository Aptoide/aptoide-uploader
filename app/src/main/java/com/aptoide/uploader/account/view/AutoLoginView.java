package com.aptoide.uploader.account.view;

import com.aptoide.uploader.view.View;
import io.reactivex.Observable;

public interface AutoLoginView extends View {
  Observable<Object> clickAutoLogin();

  Observable<Object> clickOtherLogins();

  void showLoginAvatar();

  void showLoginName();

  void showNetworkError();
}
