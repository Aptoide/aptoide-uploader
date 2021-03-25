package com.aptoide.uploader.apps.persistence

import com.aptoide.uploader.apps.AutoUploadSelects
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

open class RoomAutoUploadSelectsPersistence(
    private val autoUploadSelectsDao: AutoUploadSelectsDao) : AutoUploadSelectsPersistence {

  override fun getAllAutoUploadSelectStatus(): Observable<MutableList<AutoUploadSelects>> {
    return autoUploadSelectsDao.getAllAutoUploadSelectStatus()
        .subscribeOn(Schedulers.io())
  }

  override fun getSelectedApps(): Observable<MutableList<AutoUploadSelects>> {
    return autoUploadSelectsDao.getSelectedApps()
        .subscribeOn(Schedulers.io())
  }

  override fun isSelectedApp(installedPackageName: String): Boolean {
    return autoUploadSelectsDao.isSelectedApp(installedPackageName) > 0
  }

  override fun remove(packageName: String, versionCode: Int): Completable {
    return autoUploadSelectsDao.remove(packageName, versionCode)
        .subscribeOn(Schedulers.io())
  }

  override fun insert(autoUploadSelects: AutoUploadSelects): Completable {
    return Completable.fromAction { autoUploadSelectsDao.save(autoUploadSelects) }
        .subscribeOn(Schedulers.io())
  }

  override fun replaceAllBy(
      list: List<AutoUploadSelects>): Completable {
    return Completable.fromAction {
      autoUploadSelectsDao.removeAll()
      autoUploadSelectsDao.saveAll(list)
    }.subscribeOn(Schedulers.io())
  }
}
