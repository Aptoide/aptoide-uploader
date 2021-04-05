package com.aptoide.uploader.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.aptoide.uploader.apps.persistence.RoomAutoUploadSelectsPersistence
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AutoUploadSelectsManager(
    private val roomAutoUploadSelectsPersistence: RoomAutoUploadSelectsPersistence,
    private val packageManager: PackageManager) {

  private fun getInstalledToAutoUploadSelection(): Single<List<AutoUploadSelects>> {
    return Observable.fromIterable<ApplicationInfo>(
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
        .filter { applicationInfo -> (applicationInfo.packageName != null) and (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) }
        .map { applicationInfo ->
          val packageInfo =
              packageManager.getPackageInfo(applicationInfo.packageName, 0)
          AutoUploadSelects(packageInfo.packageName, false)
        }
        .toList()
        .subscribeOn(Schedulers.io())
  }

  fun insertAllInstalled(): Completable {
    return getInstalledToAutoUploadSelection()
        .doOnError { throwable -> Log.e("APP-86", "Error " + throwable.printStackTrace()) }
        .flatMapCompletable { installed ->
          Log.d("APP-86", "insertAllInstalled: autoUploadSelectsList size " + installed.size)
          roomAutoUploadSelectsPersistence.replaceAllBy(installed)
        }
  }
}