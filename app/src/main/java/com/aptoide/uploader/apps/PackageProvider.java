package com.aptoide.uploader.apps;

import io.reactivex.Observable;
import java.util.List;

/**
 * Created by pedroribeiro on 10/11/17.
 */

public interface PackageProvider {
  Observable<List<App>> getInstalledApps();
}
