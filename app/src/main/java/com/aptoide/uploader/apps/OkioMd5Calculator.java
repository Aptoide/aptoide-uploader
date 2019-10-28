package com.aptoide.uploader.apps;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.io.File;
import java.util.Map;
import okio.Okio;

public class OkioMd5Calculator implements Md5Calculator {
  private final Map<String, String> cache;
  private final Scheduler scheduler;

  public OkioMd5Calculator(Map<String, String> cache, Scheduler scheduler) {
    this.cache = cache;
    this.scheduler = scheduler;
  }

  @Override public Single<String> calculate(String path) {
    if (cache.containsKey(path)) {
      return Single.just(cache.get(path));
    }

    return Single.fromCallable(() -> Okio.buffer(Okio.source(new File(path)))
        .readByteString()
        .md5()
        .hex())
        .subscribeOn(scheduler)
        .doOnSuccess(md5 -> cache.put(path, md5));
  }
}
