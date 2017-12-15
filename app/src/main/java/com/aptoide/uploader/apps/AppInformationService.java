package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.ProposedAppInfo;
import io.reactivex.Single;

public interface AppInformationService {
  Single<ProposedAppInfo> getProposedAppInfo(String packageName, String language);
}
