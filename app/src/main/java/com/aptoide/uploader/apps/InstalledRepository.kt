package com.aptoide.uploader.apps

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

class InstalledRepository(private val installedPersistence: RoomInstalledPersistence,
                          private val packageManager: PackageManager) {

  fun save(installed: RoomInstalled): Completable {
    return installedPersistence.insert(installed)
  }

  operator fun contains(packageName: String): Boolean {
    return installedPersistence.isInstalled(packageName)!!
        .blockingFirst()!!

  }

  /**
   * This method assures that it returns a list of installed apps synced with the the device.
   * If it hasn't been synced yet, sync it before returning.
   * Note that it only assures that these apps were synced at least once since the app started.
   */

  private fun allInstalled(): Observable<MutableList<RoomInstalled>> {
    return installedPersistence.allInstalled()
  }

  fun getAsList(
      packageName: String): Observable<MutableList<RoomInstalled>> {
    return installedPersistence.getAllAsList(packageName)
  }

  fun getInstalled(packageName: String): Observable<RoomInstalled> {
    return installedPersistence.getInstalled(packageName)
  }

  fun remove(packageName: String, versionCode: Int): Completable {
    return installedPersistence.remove(packageName, versionCode)
  }

  fun isInstalled(packageName: String): Observable<Boolean> {
    return installedPersistence.isInstalled(packageName)
  }

  fun allInstalledSorted(): Observable<MutableList<RoomInstalled>> {
    return installedPersistence.allInstalledSorted()
  }
  operator fun get(packageName: String, versionCode: Int): Observable<RoomInstalled> {
    return installedPersistence[packageName, versionCode]
  }

  fun getAllInstalledApps(
      packageManager: PackageManager): MutableList<PackageInfo?> {
    return packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES)
  }
}