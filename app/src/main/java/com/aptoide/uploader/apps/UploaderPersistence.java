package com.aptoide.uploader.apps;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface UploaderPersistence {
  Observable<List<Upload>> getUploads();

  Completable save(Upload upload);

}
