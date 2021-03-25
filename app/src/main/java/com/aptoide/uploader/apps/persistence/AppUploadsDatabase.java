package com.aptoide.uploader.apps.persistence;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.aptoide.uploader.apps.AppUploadStatus;
import com.aptoide.uploader.apps.AutoUploadSelects;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.ObbTypeConverter;

@Database(entities = {
    AppUploadStatus.class, InstalledApp.class, AutoUploadSelects.class
}, version = 1) @TypeConverters({ ObbTypeConverter.class }) public abstract class AppUploadsDatabase
    extends RoomDatabase {
  private static volatile AppUploadsDatabase INSTANCE;

  public static AppUploadsDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (AppUploadsDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppUploadsDatabase.class,
              "AppUploadsDatabase.db")
              .build();
        }
      }
    }
    return INSTANCE;
  }

  public abstract AppUploadStatusDao appUploadsStatusDao();

  public abstract InstalledDao installedDao();

  public  abstract AutoUploadSelectsDao autoUploadSelectsDao();
}
