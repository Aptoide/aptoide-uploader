package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.AppsManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.disposables.CompositeDisposable;

public class MyAppsPresenter implements Presenter {

  private final MyAppsView view;
  private final AppsManager appsManager;
  private final CompositeDisposable compositeDisposable;

  public MyAppsPresenter(MyAppsView view, AppsManager appsManager,
      CompositeDisposable compositeDisposable) {
    this.view = view;
    this.appsManager = appsManager;
    this.compositeDisposable = compositeDisposable;
  }

  @Override public void present() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> appsManager.getStore())
        .doOnNext(apps -> view.showApps(apps))
        .subscribe());

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe());
  }
}
