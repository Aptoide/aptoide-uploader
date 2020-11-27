package com.aptoide.uploader.apps

import io.reactivex.Completable
import io.reactivex.Single

class InstallManager(private val installedRepository: InstalledRepository, private val packageManagerInstalledAppsProvider: PackageManagerInstalledAppsProvider) {
  
  fun insertAllInstalled(): Completable{
    return packageManagerInstalledAppsProvider.installedApps
        .flatMapCompletable { installed ->
          return@flatMapCompletable installedRepository.replaceAllBy(installed)
        }
  }

  fun onAppInstalled1(installed: InstalledApp): Completable {
    return installedRepository.getInstalledVersionsList(installed.packageName).firstOrError().toObservable()
        .flatMapIterable { installeds ->
          if (installeds.isEmpty()) {
            installeds.add(installed)
          }
          installeds
        }
        .flatMapCompletable { databaseInstalled ->
          if (databaseInstalled.versionCode === installed.versionCode) {
            installed.status = RoomInstalled.STATUS_COMPLETED
            return@flatMapCompletable installedRepository.save(installed)
          } else {
            return@flatMapCompletable installedRepository.remove(databaseInstalled.getPackageName(),
                databaseInstalled.versionCode)
          }
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
            installed.status = InstalledApp.STATUS_COMPLETED
            return@flatMapCompletable installedRepository.save(installed)
          } else {
            return@flatMapCompletable installedRepository.remove(databaseInstalled.getPackageName(),
                databaseInstalled.versionCode)
          }
        }
  }

  fun onAppRemoved(packageName: String): Completable {
    return installedRepository.getInstalledVersionsList(packageName)
        .flatMapIterable { installeds -> installeds }
        .flatMapCompletable { installed -> installedRepository.remove(packageName, installed.versionCode) }
  }
}