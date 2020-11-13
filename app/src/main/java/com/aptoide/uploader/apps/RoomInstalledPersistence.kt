package com.aptoide.uploader.apps
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class RoomInstalledPersistence(private val installedDao: InstalledDao?) :
    InstalledPersistence {

  override fun allApps(): Observable<MutableList<RoomInstalled?>?>? {
    return installedDao?.allInstalled()!!
        .subscribeOn(Schedulers.io())
  }
  override fun allInstalled(): Observable<MutableList<RoomInstalled?>?>? {
    return installedDao?.allInstalled()!!
        .flatMap { installs -> filterCompleted(installs) }
        .subscribeOn(Schedulers.io())
  }
  override fun allInstalledSorted(): Observable<MutableList<RoomInstalled?>?>? {
      return installedDao?.allSortedAsc()!!
          .flatMap { installs -> filterCompleted(installs) }
          .subscribeOn(Schedulers.io())
    }

  override fun remove(packageName: String?, versionCode: Int): Completable? {
    return installedDao?.remove(packageName, versionCode)!!
        .subscribeOn(Schedulers.io())
  }

  override fun isInstalled(packageName: String?): Observable<Boolean?>? {
    return getInstalled(packageName)!!.map(
        { installed -> installed != null && installed.getStatus() === RoomInstalled.STATUS_COMPLETED })
  }

  override fun getInstalled(packageName: String?): Observable<RoomInstalled?>? {
    return getInstalledAsList(packageName)!!.map { installedList ->
      if (installedList.isEmpty()) {
        return@map null
      } else {
        return@map installedList.get(0)
      }
    }
  }

  override fun get(packageName: String?, versionCode: Int): Observable<RoomInstalled?>? {
    return installedDao?.get(packageName, versionCode)!!
        .subscribeOn(Schedulers.io())
  }

  override fun getAsList(packageName: String?,
                         versionCode: Int): Observable<MutableList<RoomInstalled?>?>? {
    return installedDao?.getAsList(packageName, versionCode)!!
        .subscribeOn(Schedulers.io())
  }

  override fun insert(installed: RoomInstalled?): Completable? {
    return Completable.fromAction { installedDao!!.insert(installed) }
        .subscribeOn(Schedulers.io())
  }

  override fun getAllAsList(
      packageName: String?): Observable<MutableList<RoomInstalled?>?>? {
    return installedDao?.getAsListByPackageName(packageName)!!
        .subscribeOn(Schedulers.io())
  }

  override fun replaceAllBy(list: MutableList<RoomInstalled?>?): Completable? {
    return Completable.fromAction {
      installedDao!!.removeAll()
      installedDao.insertAll(list)
    }.subscribeOn(Schedulers.io())
  }

  override fun isInstalled(packageName: String?, versionCode: Int): Single<Boolean?>? {
    return installedDao?.isInstalledByVersion(packageName, versionCode)!!
        .onErrorReturn { null }
        .map { installed ->
          (installed.getStatus() === RoomInstalled.STATUS_COMPLETED)
        }
  }

  private fun filterCompleted(
      installs: List<RoomInstalled?>): Observable<MutableList<RoomInstalled?>>? {
    return Observable.fromIterable(installs)
        .filter{ installed -> installed.getStatus() === RoomInstalled.STATUS_COMPLETED }
        .toList()
        .toObservable()
  }

  private fun getInstalledAsList(
      packageName: String?): Observable<MutableList<RoomInstalled?>?>? {
    return installedDao!!.getAsListByPackageName(packageName)!!
        .flatMap { installs -> filterCompleted(installs) }
        .subscribeOn(Schedulers.io())
  }
}
