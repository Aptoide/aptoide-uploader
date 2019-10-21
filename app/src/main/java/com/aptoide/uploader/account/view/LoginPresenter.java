package com.aptoide.uploader.account.view;

import android.content.Context;
import android.util.Log;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginManager;
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
  private final AutoLoginManager autoLoginManager;
  private final Context context;

  public LoginPresenter(LoginView view, AptoideAccountManager accountManager,
      LoginNavigator loginNavigator, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler, UploaderAnalytics uploaderAnalytics,
      AutoLoginManager autoLoginManager, Context context) {
    this.view = view;
    this.accountManager = accountManager;
    this.loginNavigator = loginNavigator;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
    this.uploaderAnalytics = uploaderAnalytics;
    this.autoLoginManager = autoLoginManager;
    this.context = context;
  }

  @Override public void present() {

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> accountManager.getAccount())
        .observeOn(viewScheduler)
        .doOnNext(account -> {
          if (account.isLoggedIn()) {
            if (account.hasStore()) {
              loginNavigator.navigateToMyAppsView();
              //view.hideLoading();
            } else {
              loginNavigator.navigateToCreateStoreView();
              //view.hideLoading();
            }
          }
        })
        .doOnNext(__ -> view.hideLoading())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .doOnNext(__ -> view.showLoadingWithoutUserName())
        .filter(
            __ -> !((UploaderApplication) context.getApplicationContext()).getAutoLoginPersistence()
                .isForcedLogout())
        .flatMapSingle(__ -> autoLoginManager.getStoredUserCredentials()
            .flatMap(credentials -> accountManager.saveAutoLoginCredentials(credentials)))
        .firstOrError()
        .flatMapCompletable(account -> accountManager.loginWithAutoLogin(account))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> {
          Log.e(getClass().getSimpleName(), throwable.getMessage());
          ((UploaderApplication) context.getApplicationContext()).getAutoLoginPersistence()
              .setForcedLogout(true);
          accountManager.logout();
          accountManager.removeAccessTokenFromPersistence();
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(__ -> view.getLoginEvent()
            .doOnNext(credentials -> {
              view.hideKeyboard();
              view.showLoading(credentials.getUsername());
              accountManager.logout();
              accountManager.removeAccessTokenFromPersistence();
            })
            .flatMapCompletable(credentials -> accountManager.login(credentials.getUsername(),
                credentials.getPassword())
                .doOnComplete(() -> uploaderAnalytics.sendLoginEvent("email", "success")))
            .observeOn(viewScheduler)
            .doOnError(throwable -> {
              uploaderAnalytics.sendLoginEvent("email", "fail");
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
        }, throwable -> view.showNetworkError()));

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
            .doOnNext(account -> {
              view.showLoading(account.getDisplayName());
            })
            .flatMapCompletable(
                googleAccount -> accountManager.loginWithGoogle(googleAccount.getEmail(),
                    googleAccount.getServerAuthCode())))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> view.showNetworkError()));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(created -> view.facebookLoginSucessEvent()
            .doOnNext(account -> {
              view.showLoadingWithoutUserName();
            })
            .flatMapCompletable(facebookAccount -> accountManager.loginWithFacebook(null,
                facebookAccount.getAccessToken()
                    .getToken())))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> view.showNetworkError()));
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
