package com.aptoide.uploader.apps.view;

import android.util.Log;
import com.aptoide.uploader.apps.InstalledAppsManager;
import com.aptoide.uploader.apps.permission.UploadPermissionProvider;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import java.net.SocketTimeoutException;
import java.util.List;

public class AutoUploadPresenter implements Presenter {

  private final AutoUploadView view;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;
  private final AutoUploadNavigator autoUploadNavigator;
  private final UploadPermissionProvider uploadPermissionProvider;
  private final InstalledAppsManager installedAppsManager;

  public AutoUploadPresenter(AutoUploadView view, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler, AutoUploadNavigator autoUploadNavigator,
      UploadPermissionProvider uploadPermissionProvider,
      InstalledAppsManager installedAppsManager) {
    this.view = view;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
    this.autoUploadNavigator = autoUploadNavigator;
    this.uploadPermissionProvider = uploadPermissionProvider;
    this.installedAppsManager = installedAppsManager;
  }

  @Override public void present() {
    handleBackButtonClick();

    showApps();

    refreshStoreAndApps();
  }

  private void handleBackButtonClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.backToSettingsClick())
        .doOnNext(click -> autoUploadNavigator.navigateToSettingsFragment())
        .subscribe(click -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void showApps() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> installedAppsManager.getInstalledAppsStatus())
        .observeOn(viewScheduler)
        .doOnNext(installedAppsStatus -> Log.d("APP-86",
            "AutoUploadPresenter: showApps(): size " + installedAppsStatus.getApps()
                .size()))
        .doOnNext(installedAppsStatus -> view.showApps(installedAppsStatus.getApps(),
            installedAppsStatus.getAutoUploadSelects()))
        .flatMapCompletable(__ -> handleSelectedApps())
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void refreshStoreAndApps() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(event -> view.refreshEvent()
            .flatMap(refreshEvent -> installedAppsManager.getInstalledAppsStatus())
            .observeOn(viewScheduler)
            .doOnNext(installedAppsStatus -> view.refreshApps(installedAppsStatus.getApps(),
                installedAppsStatus.getAutoUploadSelects())))
        .subscribe());
  }

  private Observable<List<String>> checkSelectedApps() {
    return installedAppsManager.getSelectedFromAutoUploadSelectsPersistence()
        .observeOn(viewScheduler)
        .doOnNext(packageList -> view.getPreviousSavedSelection(packageList));
  }

  private Completable handleSelectedApps() {
    return checkSelectedApps().flatMap(__ -> view.submitSelectionClick())
        .doOnNext(__ -> uploadPermissionProvider.requestExternalStoragePermission())
        .flatMap(__ -> uploadPermissionProvider.permissionResultExternalStorage())
        .filter(granted -> granted)
        .flatMapSingle(__ -> view.getSelectedApps())
        .flatMap(selected -> view.saveSelectedOnSubmit(selected))
        .flatMapCompletable(
            changedList -> installedAppsManager.replaceSelectsListOnPersistence(changedList)
                .doOnComplete(() -> autoUploadNavigator.navigateToSettingsFragment()))
        .observeOn(viewScheduler)
        .doOnError(throwable -> {
          if (throwable instanceof SocketTimeoutException) {
            view.showError();
          }
        })
        .retry();
  }
}
