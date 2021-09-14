package com.aptoide.uploader.apps.persistence

import com.aptoide.uploader.apps.InstalledApp
import io.reactivex.Completable
import io.reactivex.Observable

interface InstalledPersistence {

  fun allInstalled(): Observable<MutableList<InstalledApp>>
  fun allInstalledSorted(): Observable<MutableList<InstalledApp>>

  fun remove(packageName: String, versionCode: Int): Completable
  fun removeAllPackageVersions(packageName: String): Completable

  fun getInstalled(packageName: String, versionCode: Int): Observable<InstalledApp>

  fun insert(installed: InstalledApp): Completable
  fun getInstalledVersionsList(
      packageName: String): Observable<MutableList<InstalledApp>>

  fun replaceAllBy(
      list: MutableList<InstalledApp>): Completable
}