package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Upload;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface UploaderService {

  Single<Upload> getAppUpload(String md5, String language, String storeName,
      InstalledApp installedApp);

  Observable<Upload> uploadAppToRepo(Upload upload);
}
