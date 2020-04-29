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

public class LoginPresenter implements Presenter {

  private final LoginView view;
  private final AptoideAccountManager accountManager;
  private final LoginNavigator loginNavigator;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;
  private final UploaderAnalytics uploaderAnalytics;
  private final AutoLoginManager autoLoginManager;

  public LoginPresenter(LoginView view, AptoideAccountManager accountManager,
      LoginNavigator loginNavigator, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler, UploaderAnalytics uploaderAnalytics,
      AutoLoginManager autoLoginManager) {
    this.view = view;
    this.accountManager = accountManager;
    this.loginNavigator = loginNavigator;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
    this.uploaderAnalytics = uploaderAnalytics;
    this.autoLoginManager = autoLoginManager;
  }

  @Override public void present() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> accountManager.getAccount())
        .observeOn(viewScheduler)
        .flatMap(account -> {
          if (account.isLoggedIn()) {
            view.showLoadingWithoutUserName();
            if (account.hasStore()) {
              loginNavigator.navigateToMyAppsView();
            } else {
              loginNavigator.navigateToCreateStoreView();
            }
          } else {
            view.hideLoading();
            return tryAutoLogin();
          }
          return Observable.empty();
        })
        .subscribe(__ -> {
        }, throwable -> {
          throwable.printStackTrace();
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
            .doOnNext(__ -> {
              accountManager.logout();
              accountManager.removeAccessTokenFromPersistence();
            })
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
                    googleAccount.getServerAuthCode())
                    .doOnComplete(() -> uploaderAnalytics.sendLoginEvent("google", "success"))))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> {
          uploaderAnalytics.sendLoginEvent("google", "fail");
          view.showNetworkError();
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(created -> view.facebookLoginSucessEvent()
            .doOnNext(account -> {
              view.showLoadingWithoutUserName();
            })
            .flatMapCompletable(facebookAccount -> accountManager.loginWithFacebook(null,
                facebookAccount.getAccessToken()
                    .getToken())
                .doOnComplete(() -> uploaderAnalytics.sendLoginEvent("facebook", "success"))))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> {
          view.showNetworkError();
          uploaderAnalytics.sendLoginEvent("facebook", "fail");
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickOnBlog())
        .doOnNext(__ -> loginNavigator.openBlogUrl())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private Observable<Object> tryAutoLogin() {
    return Observable.just(autoLoginManager.getAutologinFlag())
        .filter(flag -> !flag)
        .doOnNext(__ -> view.showLoadingWithoutUserName())
        .flatMap(__ -> autoLoginManager.getStoredUserCredentials()
            .flatMapObservable(credentials -> accountManager.saveAutoLoginCredentials(credentials)))
        .observeOn(viewScheduler)
        .flatMapCompletable(account -> accountManager.loginWithAutoLogin(account)
            .doOnComplete(() -> {
              autoLoginManager.setAutoLoginFlag(true);
              uploaderAnalytics.sendLoginEvent("auto-login", "success");
            }))
        .onErrorResumeNext(throwable -> {
          uploaderAnalytics.sendLoginEvent("auto-login", "fail");
          view.hideLoading();
          return accountManager.logout();
        })
        .andThen(Observable.empty());
  }
}
