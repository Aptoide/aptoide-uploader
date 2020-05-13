package com.aptoide.uploader.apps;

import android.content.pm.PackageManager;

import io.reactivex.Single;
import java.util.List;

public interface InstalledAppsProvider {

  Single<List<InstalledApp>> getInstalledApps();

  Single<InstalledApp> getInstalledApp(String packageName);
}
