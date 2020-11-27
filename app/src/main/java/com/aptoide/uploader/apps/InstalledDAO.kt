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
  fun allInstalled(): Observable<MutableList<InstalledApp>>

  @Query("SELECT * FROM Installed ORDER BY name ASC")
  fun allSortedAsc(): Observable<MutableList<InstalledApp>>

  @Query(
      "DELETE FROM Installed where packageName = :packageName AND versionCode = :versionCode")
  fun remove(packageName: String, versionCode: Int): Completable

  @Query(
      "SELECT * FROM Installed where packageName = :packageName AND versionCode = :versionCode LIMIT 1")
  fun getInstalled(packageName: String,
                   versionCode: Int): Observable<InstalledApp>

  @Query("SELECT * FROM Installed where packageName = :packageName")
  fun getInstalledVersionsList(
      packageName: String): Observable<MutableList<InstalledApp>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(installedList: MutableList<InstalledApp>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(roomInstalledApp: InstalledApp)

  @Query("DELETE FROM installed")
  fun removeAll()

  @Query(
      "SELECT * FROM installed where packageName = :packageName AND versionCode = :versionCode")
  fun isInstalledByVersion(packageName: String,
                           versionCode: Int): Single<InstalledApp>
}