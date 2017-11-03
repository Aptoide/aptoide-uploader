package com.aptoide.uploader;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class AccountPresenter implements Presenter {

  private final AccountView view;
  private final AptoideAccountManager accountManager;
  private final AccountNavigator accountNavigator;
  private final CompositeDisposable compositeDisposable;

  public AccountPresenter(AccountView view, AptoideAccountManager accountManager,
      AccountNavigator accountNavigator, CompositeDisposable compositeDisposable) {
    this.view = view;
    this.accountManager = accountManager;
    this.accountNavigator = accountNavigator;
    this.compositeDisposable = compositeDisposable;
  }

  @Override public void present() {

    compositeDisposable.add(view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.getLoginEvent())
        .flatMapSingle(credentials -> accountManager.login(credentials.getUsername(),
            credentials.getPassword()))
        .filter(account -> account.hasStore())
        .doOnNext(account -> accountNavigator.navigateToAppsView())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(lifecycleEvent -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }
}
