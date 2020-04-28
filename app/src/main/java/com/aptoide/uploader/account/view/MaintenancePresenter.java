package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.MaintenanceManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

public class MaintenancePresenter implements Presenter {

  private MaintenanceView view;
  private MaintenanceNavigator navigator;
  private MaintenanceManager maintenanceManager;
  private CompositeDisposable compositeDisposable;
  private Scheduler viewScheduler;

  public MaintenancePresenter(MaintenanceView view, MaintenanceNavigator navigator,
      MaintenanceManager maintenanceManager, CompositeDisposable compositeDisposable,
      Scheduler viewScheduler) {
    this.view = view;
    this.navigator = navigator;
    this.maintenanceManager = maintenanceManager;
    this.compositeDisposable = compositeDisposable;
    this.viewScheduler = viewScheduler;
  }

  @Override public void present() {
    handleLoginStatus();
    handleBlogClick();
  }

  private void handleBlogClick() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickOnBlog())
        .doOnNext(__ -> navigator.openBlogUrl())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void handleLoginStatus() {
    compositeDisposable.add(view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> maintenanceManager.logoutUser().andThen(Observable.just(true)))
        .flatMap(__-> maintenanceManager.shouldShowSocialLogin())
        .observeOn(viewScheduler)
        .doOnNext(shouldShowLogin -> {
          if (shouldShowLogin) {
            //navigate to newly edited Login fragment
            view.hideProgressBar();
            navigator.navigateToLoginFragment();
            //view.showSocialLoginMaintenanceView();
          } else {
            view.showNoLoginView();
          }
        })
        .subscribe());
  }
}
