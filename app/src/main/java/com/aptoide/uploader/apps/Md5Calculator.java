package com.aptoide.uploader.apps;

import io.reactivex.Single;

public interface Md5Calculator {

  Single<String> calculate(App app);

}
