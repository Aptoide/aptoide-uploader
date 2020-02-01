package com.aptoide.uploader.apps.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.aptoide.uploader.apps.AppUploadStatus;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

@Dao interface AppUploadStatusDao {
  @Query("SELECT * FROM AppUploads") Observable<List<AppUploadStatus>> getAppUploadsStatuses();

  @Insert(onConflict = OnConflictStrategy.REPLACE) Completable save(
      AppUploadStatus appUploadStatus);

  @Insert(onConflict = OnConflictStrategy.REPLACE) Completable saveAll(
      List<AppUploadStatus> appUploadStatusList);

  @Query("SELECT * FROM AppUploads WHERE status = :unknown")
  Observable<List<AppUploadStatus>> getAppsUnknownUploadStatus(int unknown);

  @Delete Completable remove(AppUploadStatus appUploadStatus);
}
