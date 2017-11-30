package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.StoreManager;
import android.util.Log;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class MyStorePresenter implements Presenter {

  private final MyStoreView view;
  private final StoreManager storeManager;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;

  public MyStorePresenter(MyStoreView view, StoreManager storeManager,
      CompositeDisposable compositeDisposable, Scheduler viewScheduler) {
    this.view = view;
    this.storeManager = storeManager;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
  }

  @Override public void present() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> storeManager.getStore())
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

    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.listenForAppClicks())
        .doOnNext(app -> {
          // ...or should I modify the app state here?
          Log.d("App click", "clicked app with name: " + app.getName());
        })
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }
}
