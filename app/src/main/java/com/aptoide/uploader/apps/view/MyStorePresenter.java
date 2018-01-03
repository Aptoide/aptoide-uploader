package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.StoreManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import java.util.Collections;
import java.util.List;

public class MyStorePresenter implements Presenter {

  private final MyStoreView view;
  private final StoreManager storeManager;
  private final CompositeDisposable compositeDisposable;
  private final MyStoreNavigator storeNavigator;
  private final Scheduler viewScheduler;

  public MyStorePresenter(MyStoreView view, StoreManager storeManager,
      CompositeDisposable compositeDisposable, MyStoreNavigator storeNavigator,
      Scheduler viewScheduler) {
    this.view = view;
    this.storeManager = storeManager;
    this.compositeDisposable = compositeDisposable;
    this.storeNavigator = storeNavigator;
    this.viewScheduler = viewScheduler;
  }

  @Override public void present() {
    showStoreAndApps();

    handleSubmitAppEvent();

    handleOrderByEvent();

    handleSignOutClick();

    handlePositiveDialogClick();

    onDestroyDisposeComposite();
  }

  private void handlePositiveDialogClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.positiveClick())
        .flatMapCompletable(click -> storeManager.logout()
            .observeOn(viewScheduler)
            .doOnComplete(() -> storeNavigator.navigateToLoginView())
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

  private void handleSignOutClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.logoutEvent())
        .doOnNext(click -> view.showDialog())
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
        .flatMapSingle(sortingOrder -> storeManager.getStore()
            .flatMap(store -> sort(store.getApps(), sortingOrder)))
        .observeOn(viewScheduler)
        .subscribe(apps -> view.showApps(apps), throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleSubmitAppEvent() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.submitAppEvent())
        .flatMapCompletable(apps -> storeManager.upload(apps))
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void showStoreAndApps() {
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
}
