package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.Upload;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MemoryUploaderPersistence implements UploaderPersistence {

  private Set<Upload> uploadsSet;
  private PublishSubject<List<Upload>> uploadsListSubject;

  public MemoryUploaderPersistence(Set<Upload> uploadsSet) {
    this.uploadsSet = uploadsSet;
    uploadsListSubject = PublishSubject.create();
  }

  @Override public Observable<List<Upload>> getUploads() {
    return uploadsListSubject.startWith(new ArrayList<Upload>(uploadsSet));
  }

  @Override public Completable save(Upload upload) {
    return Completable.fromAction(() -> {
      uploadsSet.add(upload);
      uploadsListSubject.onNext(new ArrayList<>(uploadsSet));
    });
  }
}
