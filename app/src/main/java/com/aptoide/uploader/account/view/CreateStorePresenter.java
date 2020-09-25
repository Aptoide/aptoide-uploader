package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.AccountValidationException;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginManager;
import com.aptoide.uploader.account.network.error.DuplicatedStoreException;
import com.aptoide.uploader.account.network.error.DuplicatedUserException;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import java.io.IOException;

public class CreateStorePresenter implements Presenter {

  private final CreateStoreView view;
  private final AptoideAccountManager accountManager;
  private final LoginNavigator accountNavigator;
  private final CompositeDisposable compositeDisposable;
  private final AccountErrorMapper accountErrorMapper;
  private final Scheduler viewScheduler;
  private final AutoLoginManager autoLoginManager;

  public CreateStorePresenter(CreateStoreView view, AptoideAccountManager accountManager,
      LoginNavigator accountNavigator, CompositeDisposable compositeDisposable,
      AccountErrorMapper accountErrorMapper, Scheduler viewScheduler,
      AutoLoginManager autoLoginManager) {
    this.view = view;
    this.accountManager = accountManager;
    this.accountNavigator = accountNavigator;
    this.compositeDisposable = compositeDisposable;
    this.accountErrorMapper = accountErrorMapper;
    this.viewScheduler = viewScheduler;
    this.autoLoginManager = autoLoginManager;
  }

  @Override public void present() {
    handlePressBack();
    handlePositiveDialogClick();
    handleCreateStoreClick();
    onDestroyClearDisposables();
  }

  private void handleCreateStoreClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(created -> view.getStoreInfo()
            .doOnNext(click -> {
              view.showLoading();
              view.hideKeyboard();
            })
            .flatMapCompletable(data -> accountManager.createStore(data.getStoreName(), data.getStoreUser(), data.getStorePassword())
                .observeOn(viewScheduler)
                .doOnComplete(() -> accountNavigator.navigateToMyAppsView()))
            .doOnError(throwable -> {
              view.hideLoading();
              if (isInternetError(throwable)) {
                view.showNetworkError();
              }
              if (invalidFieldError(throwable)) {
                view.showInvalidFieldError(accountErrorMapper.map(throwable));
              } else if (isStoreNameTaken(throwable)) {
                view.showErrorStoreAlreadyExists();
              } else if (isUserNameTaken(throwable)) {
                view.showErrorUserAlreadyExists();
              }
            })
            .retry())
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handlePositiveDialogClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.positiveClick())
        .flatMapCompletable(click -> accountManager.logout()
            .observeOn(viewScheduler)
            .doOnComplete(() -> {
              autoLoginManager.checkAvailableFieldsAndNavigateTo(accountNavigator);
            })
            .doOnError(throwable -> {
              view.dismissDialog();
              view.showError();
            })
            .retry())
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handlePressBack() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.pressBack())
        .doOnNext(click -> view.showDialog())
        .subscribe(click -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void onDestroyClearDisposables() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private boolean invalidFieldError(Throwable throwable) {
    return throwable instanceof AccountValidationException;
  }

  private boolean isUserNameTaken(Throwable throwable) {
    return throwable instanceof DuplicatedUserException;
  }

  private boolean isStoreNameTaken(Throwable throwable) {
    return throwable instanceof DuplicatedStoreException;
  }

  private boolean isInternetError(Throwable throwable) {
    return throwable instanceof IOException;
  }
}
