package com.aptoide.uploader.apps

import android.util.Log
import io.reactivex.Completable

class InstallManager(private val installedRepository: InstalledRepository,
                     private val packageManagerInstalledAppsProvider: PackageManagerInstalledAppsProvider) {

  fun insertAllInstalled(): Completable {
    return packageManagerInstalledAppsProvider.installedApps
        .doOnError { throwable -> Log.e("APP-85", "Error " + throwable.printStackTrace()) }
        .flatMapCompletable { installed ->
          Log.d("APP-85", "insertAllInstalled: installedApps size " + installed.size)
          installedRepository.replaceAllBy(installed)
        }
  }

  fun onAppInstalled(installed: InstalledApp): Completable {
    Log.d("APP-85", "onAppInstalled: packageName " + installed.packageName)
    return installedRepository.save(installed)
  }

  fun onUpdateConfirmed(installed: InstalledApp): Completable {
    Log.d("APP-85", "onUpdateConfirmed: packageName " + installed.packageName)
    return installedRepository.removeAllPackageVersions(installed.packageName)
        .andThen(installedRepository.save(installed))
  }

  fun onAppRemoved(packageName: String): Completable {
    return installedRepository.getInstalledVersionsList(packageName)
        .flatMapIterable { installeds -> installeds }
        .flatMapCompletable { installed ->
          installedRepository.remove(packageName, installed.versionCode)
        }
  }
}