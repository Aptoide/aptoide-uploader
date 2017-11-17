package com.aptoide.uploader.apps;

import io.reactivex.Single;
import java.util.List;

public interface PackageProvider {

  Single<List<App>> getInstalledApps();
}
