package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Upload;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface UploaderService {

  Single<Upload> getUpload(String md5, String language, String storeName,
      InstalledApp installedApp);

  Observable<Upload> upload(String md5, String storeName, String installedAppName,
      boolean hasProposedData, InstalledApp installedApp);

  Single<Boolean> hasApplicationMetaData(String packageName, int versionCode);

  Completable upload(String apkPath);

  Completable upload(String apkPath, Metadata metadata);
}
