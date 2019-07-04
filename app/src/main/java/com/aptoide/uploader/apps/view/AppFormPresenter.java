package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.CategoriesManager;
import com.aptoide.uploader.apps.MetadataUpload;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class AppFormPresenter implements Presenter {

  private final AppFormView view;
  private final CategoriesManager categoriesManager;
  private final Scheduler scheduler;
  private final UploaderPersistence persistence;
  private final String md5;
  private final AppFormNavigator appFormNavigator;

  public AppFormPresenter(AppFormView view, CategoriesManager categoriesManager,
      Scheduler scheduler, UploaderPersistence persistence, String md5,
      AppFormNavigator appFormNavigator) {
    this.view = view;
    this.categoriesManager = categoriesManager;
    this.scheduler = scheduler;
    this.persistence = persistence;
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
        .doOnNext(__ -> view.hideKeyboard())
        .flatMapCompletable(metadata -> persistence.getUploads()
            .flatMapIterable(upload -> upload)
            .filter(upload -> upload.getStatus()
                .equals(Upload.Status.NO_META_DATA) && upload.getMd5()
                .equals(md5))
            .flatMapCompletable(upload -> Observable.just(
                new MetadataUpload(false, upload.hasProposedData(), upload.getInstalledApp(),
                    upload.getStatus(), upload.getMd5(), upload.getStoreName(), metadata))
                .flatMapCompletable(metadataUpload -> persistence.remove(upload)
                    .doOnComplete(() -> metadataUpload.setStatus(Upload.Status.META_DATA_ADDED))
                    .toSingleDefault(metadataUpload)
                    .toObservable()
                    .flatMapCompletable(aa -> persistence.save(aa)))
                .doOnComplete(() -> appFormNavigator.navigateToMyAppsView())))
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
