package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.AppsManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class MyAppsPresenter implements Presenter {

  private final MyAppsView view;
  private final AppsManager appsManager;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;

  public MyAppsPresenter(MyAppsView view, AppsManager appsManager,
      CompositeDisposable compositeDisposable, Scheduler viewScheduler) {
    this.view = view;
    this.appsManager = appsManager;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
  }

  @Override public void present() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> appsManager.getApps())
        .observeOn(viewScheduler)
        .doOnNext(store -> view.showStoreName(store.getName()))
        .doOnNext(store -> view.showApps(store.getApps()))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> compositeDisposable.clear())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }
}
