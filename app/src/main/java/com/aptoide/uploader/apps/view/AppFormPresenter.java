package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.CategoriesManager;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;

public class AppFormPresenter implements Presenter {

  private final AppFormView view;
  private final CategoriesManager categoriesManager;
  private final Scheduler scheduler;
  private final UploadManager uploadManager;
  private final String md5;
  private final AppFormNavigator appFormNavigator;

  public AppFormPresenter(AppFormView view, CategoriesManager categoriesManager,
      Scheduler scheduler, UploadManager uploadManager, String md5,
      AppFormNavigator appFormNavigator) {
    this.view = view;
    this.categoriesManager = categoriesManager;
    this.scheduler = scheduler;
    this.uploadManager = uploadManager;
    this.md5 = md5;
    this.appFormNavigator = appFormNavigator;
  }

  @Override public void present() {
    showForm();
    handleSubmitFormClick();
    handleGetCategories();
  }

  private void handleSubmitFormClick() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.submitAppEvent())
        .doOnNext(__ -> {
          view.hideKeyboard();
          if (!view.isValidForm()) view.showMandatoryFieldError();
        })
        .filter(__ -> view.isValidForm())
        .doOnNext(__ -> appFormNavigator.navigateToMyAppsView())
        .flatMapCompletable(metadata -> uploadManager.handleNoMetadata(metadata, md5))
        .subscribe();
  }

  private void handleGetCategories() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> categoriesManager.getCategories())
        .observeOn(scheduler)
        .doOnNext(categoriesList -> view.showCategories(categoriesList))
        .subscribe();
  }

  private void showForm() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .observeOn(scheduler)
        .doOnNext(created -> view.showForm())
        .subscribe();
  }
}
