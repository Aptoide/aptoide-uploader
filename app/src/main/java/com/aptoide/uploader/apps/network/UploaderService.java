package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Upload;
import io.reactivex.Single;

public interface UploaderService {

  Single<Upload> getAppUpload(String md5, String language, String storeName,
      InstalledApp installedApp);
}
