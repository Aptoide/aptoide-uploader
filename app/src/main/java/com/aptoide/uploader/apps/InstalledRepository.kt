package com.aptoide.uploader.apps

import android.content.pm.PackageManager
import io.reactivex.Completable
import io.reactivex.Observable

class InstalledRepository(private val installedPersistence: RoomInstalledPersistence,
                          private val packageManager: PackageManager) {

  fun replaceAllBy(
      list: MutableList<InstalledApp>): Completable {
    return installedPersistence.replaceAllBy(list)
  }

  fun save(installed: InstalledApp): Completable {
    return installedPersistence.insert(installed)
  }

  fun contains(packageName: String, versionCode: Int): Boolean {
    return installedPersistence.isInstalled(packageName, versionCode).blockingGet()
  }

  private fun allInstalled(): Observable<MutableList<InstalledApp>> {
    return installedPersistence.allInstalled()
  }

  fun allInstalledSorted(): Observable<MutableList<InstalledApp>> {
    return installedPersistence.allInstalledSorted()
  }

  fun getInstalledVersionsList(
      packageName: String): Observable<MutableList<InstalledApp>> {
    return installedPersistence.getInstalledVersionsList(packageName)
  }

  fun getInstalled(packageName: String, versionCode: Int): Observable<InstalledApp> {
    return installedPersistence.getInstalled(packageName, versionCode)
  }

  fun remove(packageName: String, versionCode: Int): Completable {
    return installedPersistence.remove(packageName, versionCode)
  }

  fun removeAll(): Completable {
    return installedPersistence.removeAll()
  }

  fun removeAllPackageVersions(packageName: String): Completable {
    return installedPersistence.removeAllPackageVersions(packageName)
  }
}