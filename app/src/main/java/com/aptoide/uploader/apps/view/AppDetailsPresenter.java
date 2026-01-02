package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.AppDetailsDataProvider;
import com.aptoide.uploader.apps.InstalledAppsProvider;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class AppDetailsPresenter implements Presenter {

  private final AppDetailsView view;
  private final InstalledAppsProvider installedAppsProvider;
  private final AppDetailsDataProvider dataProvider;
  private final AppDetailsNavigator navigator;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;
  private final String packageName;

  public AppDetailsPresenter(AppDetailsView view, InstalledAppsProvider installedAppsProvider,
      AppDetailsDataProvider dataProvider, AppDetailsNavigator navigator,
      CompositeDisposable compositeDisposable, Scheduler viewScheduler, String packageName) {
    this.view = view;
    this.installedAppsProvider = installedAppsProvider;
    this.dataProvider = dataProvider;
    this.navigator = navigator;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
    this.packageName = packageName;
  }

  @Override public void present() {
    loadAppDetails();
    handleBackButton();
    disposeOnDestroy();
  }

  private void loadAppDetails() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .doOnNext(__ -> view.showLoading())
        .flatMapSingle(__ -> installedAppsProvider.getInstalledApp(packageName))
        .observeOn(viewScheduler)
        .doOnNext(app -> {
          view.hideLoading();
          view.showAppDetails(app);
          view.showAppIcon(app.getIconPath());
          
          // Calculate and show app size
          long appSize = dataProvider.calculateAppSize(app);
          view.showAppSize(dataProvider.formatFileSize(appSize));
          
          // Show app type (bundle or universal)
          boolean isBundle = dataProvider.isAppBundle(app);
          int splitCount = dataProvider.getSplitCount(app);
          view.showAppType(isBundle, splitCount);
          
          // Show file paths
          view.showFilePaths(dataProvider.getAllFilePaths(app));
          
          // Show dates
          view.showInstallDate(dataProvider.formatDate(app.getFirstInstallTime()));
          view.showUpdateDate(dataProvider.formatDate(app.getInstalledDate()));
        })
        .doOnError(throwable -> {
          view.hideLoading();
          view.showError();
        })
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleBackButton() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.backButtonClick())
        .doOnNext(__ -> navigator.navigateBack())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void disposeOnDestroy() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }
}
