package com.aptoide.uploader.apps.view;

import android.util.Log;
import com.aptoide.uploader.apps.StoreManager;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

public class AutoUploadPresenter implements Presenter {

  private final AutoUploadView view;
  private final CompositeDisposable compositeDisposable;
  private final Scheduler viewScheduler;
  private final StoreManager storeManager;
  private final AppUploadStatusPersistence persistence;
  private final UploadManager uploadManager;

  public AutoUploadPresenter(AutoUploadView view, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler, StoreManager storeManager, AppUploadStatusPersistence persistence,
      UploadManager uploadManager) {
    this.view = view;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
    this.storeManager = storeManager;
    this.persistence = persistence;
    this.uploadManager = uploadManager;
  }

  @Override public void present() {
    showApps();

    refreshStoreAndApps();
  }

  private void showApps() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> storeManager.getStore())
        .observeOn(viewScheduler)
        .doOnNext(store -> Log.d("APP-86", "AutoUpload showApps: size " + store.getApps()
            .size()))
        .doOnNext(store -> view.showApps(store.getApps()))
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
            .doOnNext(store -> view.refreshApps(store.getApps())))
        .subscribe());
  }
}
