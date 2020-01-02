package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.UploadDraft;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface DraftPersistence {
  Observable<List<UploadDraft>> getDrafts();

  Completable save(UploadDraft draft);

  Completable remove(String md5);
}
