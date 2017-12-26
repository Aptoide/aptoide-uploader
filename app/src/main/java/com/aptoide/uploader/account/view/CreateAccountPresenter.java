package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.network.error.DuplicatedStoreException;
import com.aptoide.uploader.account.network.error.DuplicatedUserException;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class CreateAccountPresenter implements Presenter {

  private final CreateAccountView view;
  private final AptoideAccountManager accountManager;
  private final CreateAccountNavigator accountNavigator;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;

  public CreateAccountPresenter(CreateAccountView view, AptoideAccountManager accountManager,
      CreateAccountNavigator accountNavigator, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler) {
    this.view = view;
    this.accountManager = accountManager;
    this.accountNavigator = accountNavigator;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
  }

  @Override public void present() {
    handleAccountCreatedEvent();
    handleCreateAccountClick();
    handleNavigateToLoginViewClick();
    handleNavigateToRecoverPassViewClick();
  }

  private void handleNavigateToLoginViewClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.getOpenLoginView())
        .observeOn(viewScheduler)
        .subscribe(__ -> accountNavigator.navigateToLoginView(), throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleNavigateToRecoverPassViewClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.getOpenRecoverPasswordView())
        .observeOn(viewScheduler)
        .subscribe(__ -> accountNavigator.navigateToRecoverPassView(), throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleAccountCreatedEvent() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> accountManager.getAccount())
        .observeOn(viewScheduler)
        .doOnNext(account -> {
          if (account.isLoggedIn()) {
            view.hideLoading();
            if (account.hasStore()) {
              accountNavigator.navigateToMyAppsView();
            }
          }
        })
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleCreateAccountClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event == View.LifecycleEvent.CREATE)
        .flatMap(__ -> view.getCreateAccountEvent())
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(
            data -> accountManager.create(data.getEmail(), data.getPassword(), data.getStoreName()))
        .observeOn(viewScheduler)
        .doOnError(throwable -> {
          view.hideLoading();

          if (isInternetError(throwable)) {
            view.showNetworkError();
          }

          if (isStoreNameTaken(throwable)) {
            view.showErrorStoreAlreadyExists();
          }

          if (isUserNameTaken(throwable)) {
            view.showErrorUserAlreadyExists();
          }
        })
        .retry()
        .subscribe(() -> accountNavigator.navigateToMyAppsView(), throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private boolean isUserNameTaken(Throwable throwable) {
    return throwable instanceof DuplicatedUserException;
  }

  private boolean isStoreNameTaken(Throwable throwable) {
    return throwable instanceof DuplicatedStoreException;
  }

  private boolean isInternetError(Throwable throwable) {
    if (throwable instanceof IllegalStateException) {
      return false;
    }
    return true;
  }
}
