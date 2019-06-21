package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.CategoriesManager;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Scheduler;

public class AppFormPresenter implements Presenter {

  private final AppFormView view;
  private final CategoriesManager categoriesManager;
  private final Scheduler scheduler;
  private final UploadManager uploadManager;
  private final UploaderPersistence persistence;
  private final String md5;

  public AppFormPresenter(AppFormView view, CategoriesManager categoriesManager,
      Scheduler scheduler, UploadManager uploadManager, UploaderPersistence persistence,
      String md5) {
    this.view = view;
    this.categoriesManager = categoriesManager;
    this.scheduler = scheduler;
    this.uploadManager = uploadManager;
    this.persistence = persistence;
    this.md5 = md5;
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
