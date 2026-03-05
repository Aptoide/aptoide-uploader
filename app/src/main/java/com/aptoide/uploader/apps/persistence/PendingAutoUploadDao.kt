package com.aptoide.uploader.apps.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aptoide.uploader.apps.PendingAutoUpload

@Dao
interface PendingAutoUploadDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(pendingAutoUpload: PendingAutoUpload)

  @Query("DELETE FROM PendingAutoUpload WHERE packageName = :packageName")
  fun remove(packageName: String)

  @Query("SELECT * FROM PendingAutoUpload")
  fun getAll(): List<PendingAutoUpload>
}
