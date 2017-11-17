package com.aptoide.uploader.apps;

import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trinkes on 17/11/2017.
 */

public class PackageManagerProvider implements PackageProvider {
  @Override public Observable<List<App>> getInstalledApps() {
    List<App> appList = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      appList.add(new App("http://lorempixel.com/20/20", "Aptoide" + i));
    }
    return Observable.just(appList);
  }
}
