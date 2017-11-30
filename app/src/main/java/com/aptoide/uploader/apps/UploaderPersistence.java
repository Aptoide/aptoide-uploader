package com.aptoide.uploader.apps;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface UploaderPersistence {
  Observable<List<UploadApp>> getUploads();

  Completable save(UploadApp uploadApp, UploadApp.Status status);

}
