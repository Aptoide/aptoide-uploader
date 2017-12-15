package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.LanguageManager;
import com.aptoide.uploader.apps.RemoteAppInformationManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

/**
 * Created by franciscoaleixo on 15/12/2017.
 */

public class AppInformationFormPresenter implements Presenter {

    private final AppInformationFormView view;
    private final RemoteAppInformationManager remoteAppInformationManager;
    private final CompositeDisposable compositeDisposable;
    private final Scheduler viewScheduler;

    public AppInformationFormPresenter(AppInformationFormView view, RemoteAppInformationManager remoteAppInformationManager, CompositeDisposable compositeDisposable, Scheduler viewScheduler) {
        this.view = view;
        this.remoteAppInformationManager = remoteAppInformationManager;
        this.compositeDisposable = compositeDisposable;
        this.viewScheduler = viewScheduler;
    }

    @Override
    public void present() {
        compositeDisposable.add(view.getLifecycleEvent()
                .filter(event -> event.equals(View.LifecycleEvent.CREATE))
                .flatMap(__ -> view.getSubmitEvent())
                .flatMapCompletable(appInfo -> remoteAppInformationManager.uploadInfo(appInfo))
                .subscribe(() -> {
                }, throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        compositeDisposable.add(view.getLifecycleEvent()
                .filter(event -> event.equals(View.LifecycleEvent.DESTROY))
                .doOnNext(__ -> compositeDisposable.clear())
                .subscribe(lifecycleEvent -> {
                }, throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

    }
}
