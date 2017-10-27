package com.aptoide.uploader.login;

import io.reactivex.Flowable;

/**
 * Created by jdandrade on 27/10/2017.
 */

public interface LoginContract {

  interface LoginView {
    Flowable<Void> loginClicked();
  }

  interface LoginInteractor {

  }

  interface LoginPresenter {

  }
}
