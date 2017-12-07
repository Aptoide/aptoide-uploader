package com.aptoide.uploader.apps;

import io.reactivex.Single;
import java.io.File;
import okio.Okio;

public class OkioMd5Calculator implements Md5Calculator {

  @Override public Single<String> calculate(InstalledApp app) {
    return Single.fromCallable(() -> Okio.buffer(Okio.sink(new File(app.getApkPath())))
        .buffer()
        .md5()
        .hex());
  }
}
