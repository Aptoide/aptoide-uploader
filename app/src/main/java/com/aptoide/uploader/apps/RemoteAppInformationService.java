package com.aptoide.uploader.apps;

import io.reactivex.Single;

public interface RemoteAppInformationService {
  Single<RemoteProposedAppInfo> getProposedAppInfo(String packageName, String language);
}
