package com.aptoide.uploader.account.view;

import android.util.Log;
import androidx.room.Room;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginManager;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.InstalledDao;
import com.aptoide.uploader.apps.RoomInstalled;
import com.aptoide.uploader.apps.RoomInstalledPersistence;
import com.aptoide.uploader.apps.persistence.AppUploadsDatabase;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.internal.operators.observable.ObservableError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

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
          throwable.printStackTrace();
          uploaderAnalytics.sendLoginEvent("google", "fail");
          view.showNetworkError();
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.getFacebookLoginEvent())
        .observeOn(viewScheduler)
        .doOnNext(__ -> view.navigateToFacebookLogin())
        .subscribe(__ -> {
        }, throwable -> {
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(created -> view.facebookLoginSuccessEvent()
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
          throwable.printStackTrace();
          view.showNetworkError();
          uploaderAnalytics.sendLoginEvent("facebook", "fail");
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.getSecureLoginTextClick()
            .doOnNext(__ -> loginNavigator.openNewAuthenticationBlogUrl())
            .retry())
        .subscribe(__ -> {
        }, throwable -> {
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.getRoomInstalledQueryDB())
        .flatMap(__ -> {
          RoomInstalledPersistence roomInstalledPersistence =new RoomInstalledPersistence(AppUploadsDatabase.getInstance(getApplicationContext()).installedDao());
          return roomInstalledPersistence.allApps();
        } )
        .concatMap(Observable::fromIterable)
        .doOnNext(installed -> Log.d("APP-85", "LoginPresent: QUERY: packageName: " + installed.getPackageName() + "\tname: "+ installed.getName()+ "\tversionName: "+installed.getVersionName()))
        .subscribe(__ -> {
        }, throwable -> {
        }));

  }
}
