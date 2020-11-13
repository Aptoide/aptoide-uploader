package com.aptoide.uploader.apps

import io.reactivex.Completable

class InstallManager(private val installedRepository: InstalledRepository) {

  fun onAppInstalled(installed: RoomInstalled): Completable? {
    return installedRepository.getAsList(installed.packageName)!!
        .flatMapIterable { installeds ->
          if (installeds.isEmpty()) {
            installeds.add(installed)
          }
          installeds
        }
        .flatMapCompletable { databaseInstalled ->
          if (databaseInstalled.getVersionCode() === installed.versionCode) {
            installed.type = databaseInstalled.getType()
            installed.status = RoomInstalled.STATUS_COMPLETED
            return@flatMapCompletable installedRepository.save(installed)
          } else {
            return@flatMapCompletable installedRepository.remove(databaseInstalled.getPackageName(),
                databaseInstalled.getVersionCode())
          }
        }
  }

  fun onUpdateConfirmed(installed: RoomInstalled?): Completable? {
    return onAppInstalled(installed!!)
  }

  fun onAppRemoved(packageName: String?): Completable? {
    return installedRepository.getAsList(packageName)!!
        .flatMapIterable { installeds -> installeds }
        .flatMapCompletable { installed -> installedRepository.remove(packageName, installed.versionCode) }
  }
}