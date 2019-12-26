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
  private final Map<String, String> pathMd5Cache;
  private final Map<String, String> md5PathCache;
  private final Scheduler scheduler;

  public OkioMd5Calculator(Map<String, String> pathMd5Cache, Map<String, String> md5PathCache,
      Scheduler scheduler) {
    this.pathMd5Cache = pathMd5Cache;
    this.md5PathCache = md5PathCache;
    this.scheduler = scheduler;
  }

  @Override public Single<String> calculate(String path) {
    if (pathMd5Cache.containsKey(path)) {
      return Single.just(pathMd5Cache.get(path));
    }
    try (HashingSink hashingSink = HashingSink.md5(Okio.blackhole());
         BufferedSource source = Okio.buffer(Okio.source(new File(path)))) {
      source.readAll(hashingSink);
      String md5 = hashingSink.hash()
          .hex();
      pathMd5Cache.put(path, md5);
      md5PathCache.put(md5, path);
      return Single.just(md5)
          .subscribeOn(scheduler);
    } catch (IOException e) {
      return Single.error(e.getCause());
    }
  }

  @Override public String getPathFromCache(String md5) {
    if (md5PathCache.containsKey(md5)) {
      return md5PathCache.get(md5);
    }
    throw new NullPointerException();
  }
}


