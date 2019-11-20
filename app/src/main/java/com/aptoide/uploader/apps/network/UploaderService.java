package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.UploadDraft;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface UploaderService {

  Single<UploadDraft> startUploadDraft(String md5, String language, String storeName,
      InstalledApp installedApp);

  Observable<UploadDraft> createDraft(String md5, InstalledApp installedApp);

  Observable<UploadDraft> setDraftStatus(UploadDraft draft, DraftStatus draftStatus);

  Observable<UploadDraft> getDraftStatus(UploadDraft draft);

  Single<Boolean> hasApplicationMetaData(int draftId);

  Observable<Upload> upload(InstalledApp installedApp, String md5, String storeName,
      String apkPath);

  Observable<Upload> upload(String md5, String storeName, String appName, InstalledApp installedApp,
      Metadata metadata);

  //Observable<Upload> upload(InstalledApp installedApp, String md5, String storeName, String obbPath,
  //    String obbType);

  //Observable<Upload> upload(String md5, String storeName, String appName, InstalledApp installedApp,
  //    Metadata metadata, String obbMainPath);
}
