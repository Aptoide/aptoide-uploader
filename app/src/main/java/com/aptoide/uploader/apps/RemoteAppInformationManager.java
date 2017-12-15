package com.aptoide.uploader.apps;

import io.reactivex.Completable;
import io.reactivex.Single;

public class RemoteAppInformationManager {
    private final RemoteAppInformationService appInfoService;
    private final LanguageManager languageManager;

    public RemoteAppInformationManager(RemoteAppInformationService appInfoService, LanguageManager languageManager) {
        this.appInfoService = appInfoService;
        this.languageManager = languageManager;
    }


    public Single<RemoteProposedAppInfo> getInfoFor(InstalledApp app) {
        return languageManager.getCurrentLanguageCode().flatMap(language -> appInfoService.getProposedAppInfo(app.getPackageName(), language)
                .onErrorResumeNext(throwable -> {
                    if (isNonExistingProposedAppInfoError(throwable)) {
                        return Single.just(new RemoteProposedAppInfo());
                    }
                    return Single.error(throwable);
                }));
    }

    public Completable uploadInfo(UserProposedAppInfo appInfo) {
        return Completable.complete();
    }

    private boolean isNonExistingProposedAppInfoError(Throwable throwable) {
        return ProposedAppInfoException.class.isAssignableFrom(throwable.getClass());
    }
}
