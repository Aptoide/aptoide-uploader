package com.aptoide.uploader.apps

import android.util.Log
import com.aptoide.uploader.analytics.UploaderAnalytics
import com.aptoide.uploader.apps.persistence.AutoUploadSelectsPersistence
import com.aptoide.uploader.apps.persistence.RoomInstalledPersistence
import io.reactivex.Completable

class InstallManager(private val installedPersistence: RoomInstalledPersistence,
                     private val selectsPersistence: AutoUploadSelectsPersistence,
                     private val packageManagerInstalledAppsProvider: PackageManagerInstalledAppsProvider,
                     private val installedAppsManager: InstalledAppsManager,
                     private val storeManager: StoreManager,
                     private val uploaderAnalytics: UploaderAnalytics) {

  fun insertAllInstalled(): Completable {
    return packageManagerInstalledAppsProvider.nonSystemInstalledApps
        .doOnError { throwable -> Log.e("APP-85", "Error " + throwable.printStackTrace()) }
        .flatMapCompletable { installed ->
          Log.d("APP-85", "insertAllInstalled: installedApps size " + installed.size)
          installedPersistence.replaceAllBy(installed)
        }
  }

  fun onAppInstalled(installed: InstalledApp): Completable {
    return if (!installed.isSystem) {
      installedPersistence.insert(installed)
          .andThen(selectsPersistence.insert(
              AutoUploadSelects(installed.packageName, false)))
    } else {
      Completable.complete()
    }
  }

  fun onUpdateConfirmed(installed: InstalledApp): Completable {
    return installedPersistence.removeAllPackageVersions(installed.packageName)
        .andThen(installedPersistence.insert(installed))
        .andThen(uploadApp(installed)).doOnComplete { uploaderAnalytics.sendSubmitAppsEvent(1) }
  }

  private fun uploadApp(installed: InstalledApp): Completable {
    return if (!installedAppsManager.isUploadedVersion(installed.packageName,
            installed.versionCode) and installedAppsManager.isSelectedApp(
            installed.packageName)) {
      storeManager.upload(installed)
    } else {
      Completable.complete()
    }.retry()
  }

  fun onAppRemoved(packageName: String): Completable {
    return installedPersistence.getInstalledVersionsList(packageName)
        .flatMapIterable { installeds -> installeds }
        .flatMapCompletable { installed ->
          installedPersistence.remove(packageName, installed.versionCode)
        }
  }
}