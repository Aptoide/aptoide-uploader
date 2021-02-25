package com.aptoide.uploader.apps

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

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

  fun isInstalled(packageName: String, versionCode: Int): Single<Boolean>
}