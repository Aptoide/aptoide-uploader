package com.aptoide.uploader.apps;

import io.reactivex.Observable;
import java.util.Collections;
import java.util.List;

/**
 * Created by trinkes on 17/11/2017.
 */

public class PackageManagerProvider implements PackageProvider {
  @Override public Observable<List<App>> getInstalledApps() {
    return Observable.just(Collections.emptyList());
  }
}
