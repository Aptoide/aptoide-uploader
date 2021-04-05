package com.aptoide.uploader.apps

import android.util.Log
import com.aptoide.uploader.apps.persistence.AutoUploadSelectsPersistence
import com.aptoide.uploader.apps.persistence.RoomInstalledPersistence
import io.reactivex.Completable

class InstallManager(private val installedPersistence: RoomInstalledPersistence,
                     private val selectsPersistence: AutoUploadSelectsPersistence,
                     private val packageManagerInstalledAppsProvider: PackageManagerInstalledAppsProvider,
                     private val installedAppsManager: InstalledAppsManager,
                     private val storeManager: StoreManager) {

  fun insertAllInstalled(): Completable {
    return packageManagerInstalledAppsProvider.nonSystemInstalledApps
        .doOnError { throwable -> Log.e("APP-85", "Error " + throwable.printStackTrace()) }
        .flatMapCompletable { installed ->
          Log.d("APP-85", "insertAllInstalled: installedApps size " + installed.size)
          installedPersistence.replaceAllBy(installed)
        }
  }

  fun onAppInstalled(installed: InstalledApp): Completable {
    Log.d("APP-85", "onAppInstalled: packageName " + installed.packageName)
    return installedPersistence.insert(installed)
        .andThen(selectsPersistence.insert(
            AutoUploadSelects(installed.packageName, false)))
  }

  fun onUpdateConfirmed(installed: InstalledApp): Completable {
    Log.d("APP-85", "onUpdateConfirmed: packageName " + installed.packageName)
    return installedPersistence.removeAllPackageVersions(installed.packageName)
        .andThen(installedPersistence.insert(installed))
        .andThen {
          if (!installedAppsManager.isUploadedVersion(installed.packageName,
                  installed.versionCode) and installedAppsManager.isSelectedApp(
                  installed.packageName)) {
            Log.d("APP-86",
                "onUpdateConfirmed: app update -> to autoupload " + installed.packageName)
            storeManager.uploadApp(installed)
          }
        }
  }

  fun onAppRemoved(packageName: String): Completable {
    return installedPersistence.getInstalledVersionsList(packageName)
        .flatMapIterable { installeds -> installeds }
        .flatMapCompletable { installed ->
          installedPersistence.remove(packageName, installed.versionCode)
        }
  }
}