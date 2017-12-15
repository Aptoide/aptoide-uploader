package com.aptoide.uploader.apps;

import io.reactivex.Completable;
import io.reactivex.Single;

public class AppInformationManager {
  private final AppInformationService appInfoService;

  public AppInformationManager(AppInformationService appInfoService) {
    this.appInfoService = appInfoService;
  }

  public Single<ProposedAppInfo> getInfoFor(String language, InstalledApp app) {
    return appInfoService.getProposedAppInfo(app.getPackageName(), language)
        .onErrorResumeNext(throwable -> {
          if (isNonExistingProposedAppInfoError(throwable)) {
            return Single.just(new ProposedAppInfo());
          }
          return Single.error(throwable);
        });
  }

  public Completable uploadInfo(ProposedAppInfo appInfo) {
    return Completable.complete();
  }

  private boolean isNonExistingProposedAppInfoError(Throwable throwable) {
    return ProposedAppInfoException.class.isAssignableFrom(throwable.getClass());
  }
}
