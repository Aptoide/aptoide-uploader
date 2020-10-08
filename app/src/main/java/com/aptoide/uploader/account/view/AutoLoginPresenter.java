package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginManager;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import java.io.IOException;

public class AutoLoginPresenter implements Presenter {

  private AutoLoginView view;
  private final AptoideAccountManager accountManager;
  private final UploaderAnalytics uploaderAnalytics;
  private final AutoLoginManager autoLoginManager;
  private AutoLoginNavigator navigator;
  private CompositeDisposable compositeDisposable;
  private Scheduler viewScheduler;

  public AutoLoginPresenter(AutoLoginView view, AptoideAccountManager accountManager,
      UploaderAnalytics uploaderAnalytics, AutoLoginManager autoLoginManager,
      AutoLoginNavigator navigator, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler) {
    this.view = view;
    this.accountManager = accountManager;
    this.uploaderAnalytics = uploaderAnalytics;
    this.autoLoginManager = autoLoginManager;
    this.navigator = navigator;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
  }

  @Override public void present() {
    showUserInfo();
    checkLoginStatus();
    handleAutoLogin();
    handleOtherLoginsClick();
    clearDisposable();
  }

  private void showUserInfo() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .observeOn(viewScheduler)
        .flatMap(__ -> {
          view.showLoginName();
          view.showLoginAvatar();
          return Observable.empty();
        })
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void checkLoginStatus() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> accountManager.getAccount())
        .observeOn(viewScheduler)
        .flatMap(account -> {
          if (account.isLoggedIn()) {
            if (account.hasStore()) {
              navigator.navigateToMyAppsView();
            } else {
              navigator.navigateToCreateStoreView();
            }
          }
          return Observable.empty();
        })
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void handleAutoLogin() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.clickAutoLogin()
            .observeOn(viewScheduler)
            .doOnNext(__ -> view.showLoginMessage())
            .flatMap(__ -> {
              accountManager.logout();
              return tryAutoLogin();
            })
            .doOnError(throwable -> {
              if (isNoNetworkError(throwable)) {
                view.showNetworkError();
              }
            })
            .retry())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void clearDisposable() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(lifecycleEvent -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleOtherLoginsClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickOtherLogins())
        .doOnNext(__ -> navigator.navigateToLoginFragment())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private Observable<Object> tryAutoLogin() {
    return autoLoginManager.fetchStoredUserCredentials()
        .flatMapObservable(accountManager::saveAutoLoginCredentials)
        .observeOn(viewScheduler)
        .flatMapCompletable(account -> accountManager.loginWithAutoLogin(account)
            .doOnComplete(() -> {
              if (autoLoginManager.getAutoLoginCredentials()
                  .getStoreName() == null || autoLoginManager.getAutoLoginCredentials()
                  .getStoreName()
                  .trim()
                  .isEmpty()) {
                navigator.navigateToCreateStoreView();
              } else {
                navigator.navigateToMyAppsView();
              }
              uploaderAnalytics.sendLoginEvent("auto-login", "success");
            }))
        .doOnError(throwable -> uploaderAnalytics.sendLoginEvent("auto-login", "fail"))
        .andThen(Observable.empty());
  }

  private boolean isNoNetworkError(Throwable throwable) {
    return throwable instanceof IOException;
  }
}
