package com.aptoide.uploader.apps.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aptoide.uploader.apps.AutoUploadSelects
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface AutoUploadSelectsDao {

  @Query("SELECT * FROM AutoUploadSelects")
  fun getAllAutoUploadSelectStatus(): Observable<MutableList<AutoUploadSelects>>

  @Query("SELECT * FROM AutoUploadSelects where isSelectedAutoUpload = 1")
  fun getSelectedApps(): Observable<MutableList<AutoUploadSelects>>

  @Query(
      "SELECT COUNT(*) FROM AutoUploadSelects where packageName = :installedPackageName AND isSelectedAutoUpload = 1")
  fun isSelectedApp(installedPackageName: String): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun save(AutoUploadSelectStatus: AutoUploadSelects)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveAll(AutoUploadSelectStatusList: List<AutoUploadSelects>)

  @Query("DELETE FROM AutoUploadSelects")
  fun removeAll()

  @Query(
      "DELETE FROM AutoUploadSelects where packageName = :packageName AND versionCode = :versionCode")
  fun remove(packageName: String, versionCode: Int): Completable
}