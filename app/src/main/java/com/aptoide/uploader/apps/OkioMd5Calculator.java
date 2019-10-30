package com.aptoide.uploader.apps;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import okio.BufferedSource;
import okio.HashingSink;
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
    try (HashingSink hashingSink = HashingSink.md5(Okio.blackhole());
         BufferedSource source = Okio.buffer(Okio.source(new File(path)))) {
      source.readAll(hashingSink);
      String md5 = hashingSink.hash()
          .hex();
      cache.put(path, md5);
      return Single.just(md5)
          .subscribeOn(scheduler);
    } catch (IOException e) {
      return Single.error(e.getCause());
    }
  }
}


