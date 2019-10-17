package com.aptoide.uploader.apps;

import android.support.annotation.NonNull;
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

  @Override public Single<String> calculate(InstalledApp app) {
    if (cache.containsKey(getKey(app))) {
      return Single.just(cache.get(getKey(app)));
    }
    return Single.fromCallable(() -> Okio.buffer(Okio.source(new File(app.getApkPath())))
        .readByteString()
        .md5()
        .hex())
        .doOnSuccess(md5 -> cache.put(getKey(app), md5))
        .subscribeOn(scheduler);
  }

  @NonNull private String getKey(InstalledApp app) {
    return app.getPackageName() + app.getVersionCode();
  }

  @Override public Single<String> calculate(String path) {

    return Single.fromCallable(() -> Okio.buffer(Okio.source(new File(path)))
        .readByteString()
        .md5()
        .hex())
        .subscribeOn(scheduler);
  }
}
