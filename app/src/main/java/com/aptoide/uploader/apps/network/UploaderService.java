package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.apps.Upload;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface UploaderService {

  Single<Upload> getUpload(String md5, String language, String storeName,
      InstalledApp installedApp);

  Observable<Upload> upload(String md5, String storeName, String installedAppName,
      InstalledApp installedApp);

  Single<Boolean> hasApplicationMetaData(String packageName, int versionCode);

  Observable<Upload> upload(InstalledApp installedApp, String md5, String storeName,
      String apkPath);

  Observable<Upload> upload(String md5, String storeName, String appName, InstalledApp installedApp,
      Metadata metadata);

  Observable<Upload> upload(InstalledApp installedApp, String md5, String storeName, String obbPath,
      String obbType);

  Observable<Upload> upload(String md5, String storeName, String appName, InstalledApp installedApp,
      Metadata metadata, String obbMainPath);
}
