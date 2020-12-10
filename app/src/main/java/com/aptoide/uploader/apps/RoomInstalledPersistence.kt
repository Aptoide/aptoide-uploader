package com.aptoide.uploader.apps

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class RoomInstalledPersistence(private val installedDao: InstalledDao) :
    InstalledPersistence {

  override fun allInstalled(): Observable<MutableList<InstalledApp>> {
    return installedDao.allInstalled()
        .subscribeOn(Schedulers.io())
  }

  override fun allInstalledSorted(): Observable<MutableList<InstalledApp>> {
    return installedDao.allSortedAsc()
        .subscribeOn(Schedulers.io())
  }

  override fun remove(packageName: String, versionCode: Int): Completable {
    return installedDao.remove(packageName, versionCode)
        .subscribeOn(Schedulers.io())
  }

  override fun getInstalled(packageName: String, versionCode: Int): Observable<InstalledApp> {
    return installedDao.getInstalled(packageName, versionCode)
        .subscribeOn(Schedulers.io())
  }

  override fun insert(installed: InstalledApp): Completable {
    return Completable.fromAction { installedDao.insert(installed) }
        .subscribeOn(Schedulers.io())
  }

  override fun getInstalledVersionsList(
      packageName: String): Observable<MutableList<InstalledApp>> {
    return installedDao.getInstalledVersionsList(packageName)
        .subscribeOn(Schedulers.io())
  }

  override fun replaceAllBy(
      list: MutableList<InstalledApp>): Completable {
    return Completable.fromAction {
      installedDao.removeAll()
      installedDao.insertAll(list)
    }.subscribeOn(Schedulers.io())
  }

  override fun isInstalled(packageName: String, versionCode: Int): Single<Boolean> {
    return installedDao.isInstalledByVersion(packageName, versionCode)
        .onErrorReturn { null }
        .map { installed ->
          (installed.status === InstalledApp.STATUS_COMPLETED)
        }
  }
}
