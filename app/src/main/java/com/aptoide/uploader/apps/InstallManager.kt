package com.aptoide.uploader.apps

import android.util.Log
import io.reactivex.Completable

class InstallManager(private val installedRepository: InstalledRepository,
                     private val packageManagerInstalledAppsProvider: PackageManagerInstalledAppsProvider) {

  fun insertAllInstalled(): Completable {
    return packageManagerInstalledAppsProvider.installedApps
        .flatMapCompletable { installed ->
          Log.d("APP-85", "insertAllInstalled: installedApps size "+  installed.size)
          installedRepository.replaceAllBy(installed)
        }
  }

  fun onAppInstalled(installed: InstalledApp): Completable {
    return installedRepository.save(installed)
  }

  fun onUpdateConfirmed(installed: InstalledApp): Completable {
    return installedRepository.getInstalledVersionsList(installed.packageName)
        .flatMapIterable { installeds ->
          if (installeds.isEmpty()) {
            installeds.add(installed)
          }
          installeds
        }
        .flatMapCompletable { databaseInstalled ->
          if (databaseInstalled.versionCode === installed.versionCode) {
            return@flatMapCompletable installedRepository.save(installed)
          } else {
            return@flatMapCompletable installedRepository.remove(databaseInstalled.packageName,
                databaseInstalled.versionCode)
          }
        }
  }

  fun onAppRemoved(packageName: String): Completable {
    return installedRepository.getInstalledVersionsList(packageName)
        .flatMapIterable { installeds -> installeds }
        .flatMapCompletable { installed ->
          installedRepository.remove(packageName, installed.versionCode)
        }
  }
}