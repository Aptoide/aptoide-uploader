package com.aptoide.uploader.apps;

import io.reactivex.Single;

public interface UploaderService {

  Single<UploadApp> getUploadApp(String md5, String packageName, String language,
      String storeName);
}
