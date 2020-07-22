package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginCredentials;
import com.aptoide.uploader.account.AutoLoginManager;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class AutoLoginPresenter implements Presenter {

  private AutoLoginView view;
  private final AptoideAccountManager accountManager;
  private final UploaderAnalytics uploaderAnalytics;
  private final AutoLoginManager autoLoginManager;
  private AutoLoginNavigator navigator;
  private CompositeDisposable compositeDisposable;
  private Scheduler viewScheduler;
  private AutoLoginCredentials autoLoginCredentials;

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
    view.showName();
    handleAutoLogin();
    handleOtherLoginsClick();
    clearDisposable();
  }

  private void handleAutoLogin() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.clickAutoLogin())
        .observeOn(viewScheduler)
        .flatMap(__ -> {
          accountManager.logout();
          accountManager.removeAccessTokenFromPersistence();
          return tryAutoLogin();
        })
        .subscribe());
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
        .doOnNext(__ -> navigator.navigateToOtherLogins())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private Observable<Object> tryAutoLogin() {
    return Observable.just(accountManager.getAccount())
        .flatMap(__ -> autoLoginManager.getStoredUserCredentials()
            .flatMapObservable(credentials -> accountManager.saveAutoLoginCredentials(credentials)))
        .observeOn(viewScheduler)
        .flatMapCompletable(account -> accountManager.loginWithAutoLogin(account)
            .doOnComplete(() -> {
              navigator.navigateToMyAppsView();
              uploaderAnalytics.sendLoginEvent("auto-login", "success");
            }))
        .onErrorResumeNext(throwable -> {
          uploaderAnalytics.sendLoginEvent("auto-login", "fail");
          return accountManager.logout();
        })
        .andThen(Observable.empty());
  }
}
