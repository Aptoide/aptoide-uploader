package com.aptoide.uploader.apps.persistence;

import com.aptoide.uploader.apps.Upload;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryUploaderPersistence implements UploaderPersistence {

  private final Map<Integer, Upload> uploadsMap;
  private final PublishSubject<List<Upload>> uploadsListSubject;
  private final Scheduler scheduler;

  public MemoryUploaderPersistence(Map<Integer, Upload> uploadsMap, Scheduler scheduler) {
    this.uploadsMap = uploadsMap;
    this.scheduler = scheduler;
    uploadsListSubject = PublishSubject.create();
  }

  @Override public Observable<List<Upload>> getUploads() {
    return uploadsListSubject.startWith(new ArrayList<Upload>(uploadsMap.values()));
  }

  @Override public Completable save(Upload upload) {
    return Completable.fromAction(() -> {
      uploadsMap.put(upload.hashCode(), upload);
      uploadsListSubject.onNext(new ArrayList<>(uploadsMap.values()));
    })
        .subscribeOn(scheduler);
  }
}
