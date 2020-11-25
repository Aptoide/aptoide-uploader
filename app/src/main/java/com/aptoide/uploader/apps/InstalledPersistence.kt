package com.aptoide.uploader.apps

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface InstalledPersistence {

  fun allApps(): Observable<MutableList<RoomInstalled>>
  fun allInstalled(): Observable<MutableList<RoomInstalled>>
  fun allInstalledSorted(): Observable<MutableList<RoomInstalled>>

  fun remove(packageName: String, versionCode: Int): Completable
  fun isInstalled(packageName: String): Observable<Boolean>
  fun getInstalled(packageName: String): Observable<RoomInstalled>
  operator fun get(packageName: String, versionCode: Int): Observable<RoomInstalled>
  fun getAsList(packageName: String,
                versionCode: Int): Observable<MutableList<RoomInstalled>>

  fun insert(installed: RoomInstalled): Completable
  fun getAllAsList(
      packageName: String): Observable<MutableList<RoomInstalled>>

  fun replaceAllBy(list: MutableList<RoomInstalled>): Completable

  fun isInstalled(packageName: String, versionCode: Int): Single<Boolean>
}