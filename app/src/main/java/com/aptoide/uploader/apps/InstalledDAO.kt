package com.aptoide.uploader.apps

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface InstalledDao {
  @Query("SELECT * FROM Installed")
  fun allInstalled(): Observable<MutableList<RoomInstalled?>?>?

  @Query("SELECT * FROM Installed ORDER BY name ASC")
  fun allSortedAsc(): Observable<MutableList<RoomInstalled?>?>?

  @Query(
      "DELETE FROM Installed where packageName = :packageName AND versionCode = :versionCode")
  fun remove(packageName: String?, versionCode: Int): Completable?

  @Query(
      "SELECT * FROM Installed where packageName = :packageName AND versionCode = :versionCode LIMIT 1")
  fun get(packageName: String?,
                   versionCode: Int): Observable<RoomInstalled?>?

  @Query(
      "SELECT * FROM Installed where packageName = :packageName AND versionCode = :versionCode")
  fun getAsList(packageName: String?,
                versionCode: Int): Observable<MutableList<RoomInstalled?>?>?

  @Query("SELECT * FROM Installed where packageName = :packageName")
  fun getAsListByPackageName(
      packageName: String?): Observable<MutableList<RoomInstalled?>?>?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(installedList: MutableList<RoomInstalled?>?)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(roomInstalled: RoomInstalled?)

  @Query("DELETE FROM installed")
  fun removeAll()

  @Query(
      "SELECT * FROM installed where packageName = :packageName AND versionCode = :versionCode")
  fun isInstalledByVersion(packageName: String?,
                           versionCode: Int): Single<RoomInstalled?>?
}