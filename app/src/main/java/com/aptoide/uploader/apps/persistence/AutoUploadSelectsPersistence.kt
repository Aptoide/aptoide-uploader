package com.aptoide.uploader.apps.persistence

import com.aptoide.uploader.apps.AutoUploadSelects
import io.reactivex.Completable
import io.reactivex.Observable

interface AutoUploadSelectsPersistence {
  fun getAllAutoUploadSelectStatus(): Observable<MutableList<AutoUploadSelects>>

  fun getSelectedApps(): Observable<MutableList<AutoUploadSelects>>

  fun isSelectedApp(installedPackageName: String): Boolean

  fun remove(packageName: String, versionCode: Int): Completable

  fun insert(installed: AutoUploadSelects): Completable

  fun replaceAllBy(list: List<AutoUploadSelects>): Completable
}