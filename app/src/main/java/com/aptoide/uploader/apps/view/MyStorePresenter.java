package com.aptoide.uploader.apps.view;

import android.util.Log;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginManager;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.AppUploadStatus;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.StoreManager;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.network.ConnectivityProvider;
import com.aptoide.uploader.apps.network.NoConnectivityException;
import com.aptoide.uploader.apps.permission.UploadPermissionProvider;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

public class MyStorePresenter implements Presenter {

  private final MyStoreView view;
  private final StoreManager storeManager;
  private final CompositeDisposable compositeDisposable;
  private final MyStoreNavigator storeNavigator;
  private final Scheduler viewScheduler;
  private final UploadPermissionProvider uploadPermissionProvider;
  private final AppUploadStatusPersistence persistence;
  private final UploaderAnalytics uploaderAnalytics;
  private final ConnectivityProvider connectivityProvider;
  private final UploadManager uploadManager;
  private final AutoLoginManager autoLoginManager;
  private final AptoideAccountManager accountManager;

  public MyStorePresenter(MyStoreView view, StoreManager storeManager,
      CompositeDisposable compositeDisposable, MyStoreNavigator storeNavigator,
      Scheduler viewScheduler, UploadPermissionProvider uploadPermissionProvider,
      AppUploadStatusPersistence persistence, UploaderAnalytics uploaderAnalytics,
      ConnectivityProvider connectivityProvider, UploadManager uploadManager,
      AutoLoginManager autoLoginManager, AptoideAccountManager accountManager) {
    this.connectivityProvider = connectivityProvider;
    this.view = view;
    this.storeManager = storeManager;
    this.compositeDisposable = compositeDisposable;
    this.storeNavigator = storeNavigator;
    this.viewScheduler = viewScheduler;
    this.uploadPermissionProvider = uploadPermissionProvider;
    this.persistence = persistence;
    this.uploaderAnalytics = uploaderAnalytics;
    this.uploadManager = uploadManager;
    this.autoLoginManager = autoLoginManager;
    this.accountManager = accountManager;
  }

  @Override public void present() {
    checkFirstRun();

    showApps();

    showAvatarPath();

    showStoreName();

    refreshStoreAndApps();

    handleSubmitAppEvent();

    handleOrderByEvent();

    handleSettingsClick();

    onDestroyDisposeComposite();

    checkUploadedApps();
  }

  private void checkFirstRun() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .doOnNext(__ -> view.checkFirstRun())
        .observeOn(viewScheduler)
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void showApps() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> storeManager.getStore())
        .observeOn(viewScheduler)
        .doOnNext(store -> view.showApps(store.getApps()))
        .flatMap(__ -> checkUploadedApps())
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

  private void refreshStoreAndApps() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.refreshEvent()
            .flatMapSingle(refreshEvent -> storeManager.getStore())
            .doOnNext(refresh -> uploadManager.fillAppUploadStatusPersistence())
            .observeOn(viewScheduler)
            .doOnNext(store -> view.refreshApps(store.getApps()))
            .flatMap(apps -> checkUploadedApps()))
        .subscribe());
  }

  private void handleSettingsClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.goToSettings())
        .doOnNext(click -> storeNavigator.navigateToSettingsFragment())
        .doOnNext(__ -> setPersistenceStatus())
        .subscribe(click -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void onDestroyDisposeComposite() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleOrderByEvent() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.orderByEvent())
        .doOnNext(order -> view.orderApps(order))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleSubmitAppEvent() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.submitAppEvent()
            .flatMap(__ -> {
              if (connectivityProvider.hasConnectivity()) {
                return Observable.just(true);
              } else {
                view.showNoConnectivityError();
                return Observable.just(false);
              }
            })
            .filter(hasConnection -> hasConnection)
            .doOnNext(apps -> uploadPermissionProvider.requestExternalStoragePermission())
            .retry())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> uploadPermissionProvider.permissionResultExternalStorage())
        .filter(granted -> granted)
        .flatMapSingle(__ -> view.getSelectedApps())
        .doOnNext(__ -> view.clearSelection())
        .flatMapCompletable(apps -> storeManager.upload(apps)
            .doOnComplete(() -> uploaderAnalytics.sendSubmitAppsEvent(apps.size())))
        .observeOn(viewScheduler)
        .doOnError(throwable -> {
          if (throwable instanceof SocketTimeoutException) {
            view.showError();
          }
        })
        .retry()
        .subscribe(() -> {
        }, throwable -> {
          if (throwable instanceof NoConnectivityException) {
            Log.e(getClass().getSimpleName(), "NO INTERNET AVAILABLE!");
          }
          throwable.printStackTrace();
        }));
  }

  private Single<List<InstalledApp>> sort(List<InstalledApp> apps, SortingOrder sortingOrder) {
    if (sortingOrder.equals(SortingOrder.DATE)) {
      Collections.sort(apps,
          (app1, app2) -> Long.compare(app2.getInstalledDate(), app1.getInstalledDate()));
    } else if (sortingOrder.equals(SortingOrder.NAME)) {
      Collections.sort(apps, (app1, app2) -> app1.getName()
          .toLowerCase()
          .compareTo(app2.getName()
              .toLowerCase()));
    }
    return Single.just(apps);
  }

  private Observable<List<String>> checkUploadedApps() {
    return getAppUploadStatusFromPersistence().observeOn(viewScheduler)
        .doOnNext(packageList -> view.setCloudIcon(packageList));
  }

  private Observable<List<String>> getAppUploadStatusFromPersistence() {
    return persistence.getAppsUploadStatus()
        .distinctUntilChanged((previous, current) -> !appsPersistenceHasChanged(previous, current))
        .flatMapSingle(apps -> Observable.fromIterable(apps)
            .filter(app -> app.isUploaded())
            .map(appUploadStatus -> appUploadStatus.getPackageName())
            .toList());
  }

  private boolean appsPersistenceHasChanged(List<AppUploadStatus> previousList,
      List<AppUploadStatus> currentList) {
    if (previousList.size() != currentList.size()) {
      return true;
    }
    for (AppUploadStatus previous : previousList) {
      AppUploadStatus current = currentList.get(previousList.indexOf(previous));
      if (!previous.getMd5()
          .equals(current.getMd5()) && !(previous.isUploaded() == current.isUploaded())) {
        return true;
      }
    }
    return !previousList.equals(currentList);
  }

  private Completable setPersistenceStatus() {
    return persistence.getAppsUploadStatus()
        .flatMap(apps -> Observable.fromIterable(apps)
            .doOnNext(app -> app.setStatus(AppUploadStatus.Status.UNKNOWN)))
        .ignoreElements();
  }
}
