package com.aptoide.uploader.apps.persistence;

import android.util.Log;
import com.aptoide.uploader.apps.UploadDraft;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryDraftPersistence implements DraftPersistence {

  private final Map<String, UploadDraft> draftsMap;
  private final PublishSubject<List<UploadDraft>> draftsListSubject;
  private final Scheduler scheduler;

  public MemoryDraftPersistence(Map<String, UploadDraft> draftsMap, Scheduler scheduler) {
    this.draftsMap = draftsMap;
    this.scheduler = scheduler;
    draftsListSubject = PublishSubject.create();
  }

  @Override public Observable<List<UploadDraft>> getDrafts() {
    return draftsListSubject.startWith(new ArrayList<UploadDraft>(draftsMap.values()))
        .subscribeOn(scheduler);
  }

  @Override public Completable save(UploadDraft draft) {
    return Completable.fromAction(() -> {
      draftsMap.put(draft.getMd5(), draft);
      draftsListSubject.onNext(new ArrayList<>(draftsMap.values()));
    })
        .subscribeOn(scheduler)
        .doOnError(throwable -> Log.e("ERROR Save", throwable.getMessage()));
  }

  @Override public Completable remove(String md5) {
    return Completable.fromAction(() -> {
      draftsMap.remove(md5);
      draftsListSubject.onNext(new ArrayList<>(draftsMap.values()));
    })
        .subscribeOn(scheduler)
        .doOnError(throwable -> Log.e("ERROR Remove", throwable.getMessage()));
  }
}