package com.aptoide.uploader;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by pedroribeiro on 10/11/17.
 */

public class InstalledAppsPresenter implements Presenter {

  private final InstalledAppsView view;
  private final AppsManager appsManager;
  private final CompositeDisposable compositeDisposable;

  public InstalledAppsPresenter(InstalledAppsView view, AppsManager appsManager,
      CompositeDisposable compositeDisposable) {
    this.view = view;
    this.appsManager = appsManager;
    this.compositeDisposable = compositeDisposable;
  }

  @Override public void present() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> appsManager.getInstalledApps())
        .doOnNext(apps -> view.showApps(apps))
        .subscribe());

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe());
  }
}
