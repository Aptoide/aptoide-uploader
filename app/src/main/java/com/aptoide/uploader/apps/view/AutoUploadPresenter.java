package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.InstalledAppsManager;
import com.aptoide.uploader.apps.permission.UploadPermissionProvider;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
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

    handleSubmitApp();

    disposeComposite();
  }

  private void disposeComposite() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(click -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
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
        .doOnNext(installedAppsStatus -> view.showApps(installedAppsStatus.getInstalledApps(),
            installedAppsStatus.getAutoUploadSelects()))
        .flatMap(__ -> loadSelectedApps())
        .subscribe(__ -> {
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
            .doOnNext(
                installedAppsStatus -> view.refreshApps(installedAppsStatus.getInstalledApps(),
                    installedAppsStatus.getAutoUploadSelects())))
        .subscribe());
  }

  private Observable<List<String>> loadSelectedApps() {
    return installedAppsManager.getSelectedFromAutoUploadSelectsPersistence()
        .observeOn(viewScheduler)
        .doOnNext(packageList -> view.loadPreviousAppsSelection(packageList));
  }

  private void handleSubmitApp() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.submitSelectionClick()
            .doOnNext(_1 -> uploadPermissionProvider.requestExternalStoragePermission())
            .retry())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapCompletable(_2 -> uploadPermissionProvider.permissionResultExternalStorage()
            .filter(granted -> granted)
            .flatMapSingle(_3 -> view.getSelectedApps())
            .flatMap(selected -> view.getAutoUploadSelectedApps(selected))
            .flatMapCompletable(
                changedList -> installedAppsManager.updateAutoUploadApps(changedList)
                    .doOnComplete(() -> autoUploadNavigator.navigateToSettingsFragment()))
            .retry())
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> {
          if (throwable instanceof SocketTimeoutException) {
            view.showError();
          }
          throwable.printStackTrace();
        }));
  }
}
