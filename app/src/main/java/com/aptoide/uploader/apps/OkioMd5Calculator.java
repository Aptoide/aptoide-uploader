package com.aptoide.uploader.apps;

import android.support.annotation.NonNull;
import io.reactivex.Single;
import java.io.File;
import java.util.Map;
import okio.Okio;

public class OkioMd5Calculator implements Md5Calculator {
  private final Map<String, String> cache;

  public OkioMd5Calculator(Map<String, String> cache) {
    this.cache = cache;
  }

  @Override public Single<String> calculate(InstalledApp app) {
    if (cache.containsKey(getKey(app))) {
      return Single.just(cache.get(getKey(app)));
    }
    return Single.fromCallable(() -> Okio.buffer(Okio.source(new File(app.getApkPath())))
        .readByteString()
        .md5()
        .hex())
        .doOnSuccess(md5 -> cache.put(getKey(app), md5));
  }

  @NonNull private String getKey(InstalledApp app) {
    return app.getPackageName() + app.getVersionCode();
  }
}
