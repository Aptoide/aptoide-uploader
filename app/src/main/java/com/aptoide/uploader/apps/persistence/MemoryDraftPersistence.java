package com.aptoide.uploader.apps.persistence;

import android.util.Log;
import com.aptoide.uploader.apps.UploadDraft;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryDraftPersistence implements DraftPersistence {

  private final Map<String, UploadDraft> draftsMap;
  private final BehaviorSubject<List<UploadDraft>> draftsListSubject;
  private final Scheduler scheduler;

  public MemoryDraftPersistence(Map<String, UploadDraft> draftsMap, Scheduler scheduler) {
    this.draftsMap = draftsMap;
    this.scheduler = scheduler;
    draftsListSubject = BehaviorSubject.create();
  }

  @Override public Observable<List<UploadDraft>> getDrafts() {
    return draftsListSubject.subscribeOn(scheduler);
  }

  @Override public Completable save(UploadDraft draft) {
    return Completable.fromAction(() -> {
      UploadDraft uploadDraft = draftsMap.get(draft.getMd5());
      if (uploadDraft != null) {
        if (!draft.getStatus()
            .equals(UploadDraft.Status.IN_QUEUE) && !uploadDraft.getStatus()
            .equals(draft.getStatus())) {
          draftsMap.put(draft.getMd5(), draft);
          draftsListSubject.onNext(new ArrayList<>(draftsMap.values()));
        }
      } else {
        draftsMap.put(draft.getMd5(), draft);
        draftsListSubject.onNext(new ArrayList<>(draftsMap.values()));
      }
    })
        .subscribeOn(scheduler)
        .doOnError(throwable -> Log.e("ERROR Save", throwable.getMessage()));
  }

  @Override public Completable remove(String md5) {
    return Completable.fromAction(() -> {
      draftsMap.remove(md5);
    })
        .subscribeOn(scheduler)
        .doOnError(throwable -> Log.e("ERROR Remove", throwable.getMessage()));
  }
}
