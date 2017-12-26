package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.Upload;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface UploaderPersistence {
  Observable<List<Upload>> getUploads();

  Completable save(Upload upload);

}
