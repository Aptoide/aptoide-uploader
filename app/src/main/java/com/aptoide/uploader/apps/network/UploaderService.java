package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.UploadDraft;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public interface UploaderService {

  Single<UploadDraft> startUploadDraft(String md5, String language, String storeName,
      InstalledApp installedApp);

  Observable<UploadDraft> createDraft(String md5, InstalledApp installedApp);

  Observable<UploadDraft> setDraftStatus(UploadDraft draft, DraftStatus draftStatus);

  Observable<UploadDraft> getDraftStatus(UploadDraft draft);

  Single<Boolean> hasApplicationMetaData(UploadDraft draft);

  Observable<UploadDraft> setDraftMetadata(UploadDraft draft);

  Observable<UploadDraft> uploadFiles(UploadDraft draft);

  Observable<UploadDraft> uploadSplits(UploadDraft draft, List<String> paths);

  Observable<UploadDraft> setDraftMd5s(UploadDraft draft);

}
