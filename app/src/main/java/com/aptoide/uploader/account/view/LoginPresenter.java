package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.network.NoConnectivityException;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class LoginPresenter implements Presenter {

  private final LoginView view;
  private final AptoideAccountManager accountManager;
  private final LoginNavigator loginNavigator;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;
  private final UploaderAnalytics uploaderAnalytics;

  public LoginPresenter(LoginView view, AptoideAccountManager accountManager,
      LoginNavigator loginNavigator, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler, UploaderAnalytics uploaderAnalytics) {
    this.view = view;
    this.accountManager = accountManager;
    this.loginNavigator = loginNavigator;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
    this.uploaderAnalytics = uploaderAnalytics;
  }

  @Override public void present() {

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> accountManager.getAccount())
        .observeOn(viewScheduler)
        .doOnNext(account -> {
          if (account.isLoggedIn()) {
            view.hideLoading();
            if (account.hasStore()) {
              loginNavigator.navigateToMyAppsView();
            } else {
              loginNavigator.navigateToCreateStoreView();
            }
          }
        })
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(__ -> view.getLoginEvent()
            .doOnNext(credentials -> {
              view.hideKeyboard();
              view.showLoading(credentials.getUsername());
            })
            .flatMapCompletable(credentials -> accountManager.login(credentials.getUsername(),
                credentials.getPassword())
                .doOnComplete(() -> uploaderAnalytics.loginEvent("email", "success")))
            .observeOn(viewScheduler)
            .doOnError(throwable -> {
              uploaderAnalytics.loginEvent("email", "fail");
              view.hideLoading();
              if (isConnectivityError(throwable)) {
                view.showNoConnectivityError();
              } else if (isInternetError(throwable)) {
                view.showNetworkError();
              } else {
                view.showCrendentialsError();
              }
            })
            .retry())
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.getOpenCreateAccountView())
        .observeOn(viewScheduler)
        .subscribe(__ -> loginNavigator.navigateToCreateAccountView(), throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(lifecycleEvent -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.getGoogleLoginEvent()
            .doOnNext(__ -> view.startGoogleActivity()))
        .subscribe());

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(created -> view.googleLoginSuccessEvent()
            .flatMapCompletable(
                googleAccount -> accountManager.loginWithGoogle(googleAccount.getEmail(),
                    googleAccount.getServerAuthCode())))
        .observeOn(viewScheduler)
        .subscribe());

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(created -> view.facebookLoginSucessEvent()
            .flatMapCompletable(facebookAccount -> accountManager.loginWithFacebook(null,
                facebookAccount.getAccessToken()
                    .getToken())))
        .observeOn(viewScheduler)
        .subscribe());
  }

  private boolean isInternetError(Throwable throwable) {
    if (throwable instanceof IllegalStateException) {
      return false;
    }
    return true;
  }

  private boolean isConnectivityError(Throwable throwable) {
    if (throwable instanceof NoConnectivityException) {
      return true;
    }
    return false;
  }
}
