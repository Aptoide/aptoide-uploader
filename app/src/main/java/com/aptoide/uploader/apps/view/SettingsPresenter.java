package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginManager;
import com.aptoide.uploader.apps.StoreManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class SettingsPresenter implements Presenter {

  private final CompositeDisposable compositeDisposable;
  private final SettingsView view;
  private final Scheduler viewScheduler;
  private final AutoLoginManager autoLoginManager;
  private final AptoideAccountManager accountManager;
  private final StoreManager storeManager;
  private final SettingsNavigator settingsNavigator;

  public SettingsPresenter(CompositeDisposable compositeDisposable, SettingsView view,
      Scheduler viewScheduler, AutoLoginManager autoLoginManager,
      AptoideAccountManager accountManager, StoreManager storeManager,
      SettingsNavigator settingsNavigator) {
    this.compositeDisposable = compositeDisposable;
    this.view = view;
    this.viewScheduler = viewScheduler;
    this.autoLoginManager = autoLoginManager;
    this.accountManager = accountManager;
    this.storeManager = storeManager;
    this.settingsNavigator = settingsNavigator;
  }

  @Override public void present() {
    showAvatarPath();
    showStoreName();
    handleSignOutClick();
    handlePositiveDialogClick();

    handleBackButtonClick();
    handleAutoUploadClick();
    handleSendFeedbackClick();
    handleAboutUsClick();
    handleTermsConditionsClick();
    handlePrivacyPolicyClick();
  }

  private void showAvatarPath() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> accountManager.getAccount()
            .firstOrError()
            .map(aptoideAccount -> aptoideAccount.getAvatarPath())
            .toObservable())
        .observeOn(viewScheduler)
        .doOnNext(avatarPath -> view.showAvatar(avatarPath))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void showStoreName() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> accountManager.getAccount()
            .firstOrError()
            .map(aptoideAccount -> aptoideAccount.getStoreName())
            .toObservable())
        .observeOn(viewScheduler)
        .doOnNext(store -> view.showStoreName(store))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleSignOutClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.signOutClick())
        .doOnNext(click -> view.showDialog())
        .subscribe(click -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handlePositiveDialogClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.positiveClick())
        .flatMapCompletable(click -> storeManager.logout()
            .observeOn(viewScheduler)
            .doOnComplete(
                () -> autoLoginManager.checkAvailableFieldsAndNavigateTo(settingsNavigator))
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

  private void handleBackButtonClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.backToMyStoreClick())
        .doOnNext(click -> settingsNavigator.navigateToMyStoreFragment())
        .subscribe(click -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleAutoUploadClick() {
  }

  private void handleSendFeedbackClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.sendFeedbackClick()
            .doOnNext(__ -> settingsNavigator.openSendFeedbackUrl())
            .retry())
        .subscribe(__ -> {
        }, throwable -> {
        }));
  }

  private void handleAboutUsClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.aboutUsClick()
            .doOnNext(__ -> settingsNavigator.openAboutUsUrl())
            .retry())
        .subscribe(__ -> {
        }, throwable -> {
        }));
  }

  private void handleTermsConditionsClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.termsConditionsClick()
            .doOnNext(__ -> settingsNavigator.openTermsConditionsUrl())
            .retry())
        .subscribe(__ -> {
        }, throwable -> {
        }));
  }

  private void handlePrivacyPolicyClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.privacyPolicyClick()
            .doOnNext(__ -> settingsNavigator.openPrivacyPolicyUrl())
            .retry())
        .subscribe(__ -> {
        }, throwable -> {
        }));
  }
}
