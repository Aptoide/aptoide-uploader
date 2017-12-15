package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.Upload;
import io.reactivex.Single;

public interface UploaderService {

  Single<Upload> getAppUpload(String md5, String packageName, String language,
      String storeName);
}
