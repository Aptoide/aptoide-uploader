package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.MaintenanceManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.disposables.CompositeDisposable;

public class MaintenancePresenter implements Presenter {

  private MaintenanceView view;
  private MaintenanceNavigator navigator;
  private MaintenanceManager maintenanceManager;
  private CompositeDisposable compositeDisposable;

  public MaintenancePresenter(MaintenanceView view, MaintenanceNavigator navigator,
      MaintenanceManager maintenanceManager, CompositeDisposable compositeDisposable) {
    this.view = view;
    this.navigator = navigator;
    this.maintenanceManager = maintenanceManager;
    this.compositeDisposable = compositeDisposable;
  }

  @Override public void present() {
    handleLoginStatus();
  }

  private void handleLoginStatus() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> maintenanceManager.shouldShowSocialLogin())
        .doOnNext(shouldShowLogin -> {
          if (shouldShowLogin) {
            view.showSocialLoginMaintenanceView();
          } else {
            view.showNoLoginView();
          }
        })
        .subscribe());
  }
}
