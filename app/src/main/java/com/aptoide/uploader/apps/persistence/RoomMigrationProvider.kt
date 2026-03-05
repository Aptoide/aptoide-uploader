package com.aptoide.uploader.apps.persistence

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class RoomMigrationProvider {
  val migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
          "CREATE TABLE IF NOT EXISTS `Installed` (`packageAndVersionCode` TEXT NOT NULL, `packageName` TEXT, `name` TEXT, `versionName` TEXT, `versionCode` INTEGER NOT NULL, `isSystem` INTEGER NOT NULL, `apkPath` TEXT, `iconPath` TEXT, `installedDate` INTEGER NOT NULL, PRIMARY KEY(`packageAndVersionCode`))")

      database.execSQL(
          "CREATE TABLE IF NOT EXISTS AppUploadStatus_tmp (`md5` TEXT, `packageName` TEXT NOT NULL, `versionCode` INTEGER NOT NULL, `status` INTEGER, PRIMARY KEY(`packageName`))")
      database.execSQL(
          "INSERT INTO AppUploadStatus_tmp (md5, packageName, versionCode, status) SELECT md5,packageName,versionCode, status FROM AppUploads")
      database.execSQL("DROP TABLE AppUploads")
      database.execSQL("ALTER TABLE AppUploadStatus_tmp RENAME TO AppUploads")

      database.execSQL(
          "CREATE TABLE IF NOT EXISTS `AutoUploadSelects` (`packageName` TEXT NOT NULL, `isSelectedAutoUpload` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
    }
  }

  val migration2to3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
          "ALTER TABLE `Installed` ADD COLUMN `firstInstallTime` INTEGER NOT NULL DEFAULT 0")
      database.execSQL(
          "ALTER TABLE `Installed` ADD COLUMN `targetSdkVersion` INTEGER NOT NULL DEFAULT 0")
      database.execSQL(
          "ALTER TABLE `Installed` ADD COLUMN `minSdkVersion` INTEGER NOT NULL DEFAULT 0")
    }
  }

  val migration3to4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
          "CREATE TABLE IF NOT EXISTS `PendingAutoUpload` (`packageName` TEXT NOT NULL, `createdTimestamp` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
    }
  }

  fun getAllMigrations(): Array<Migration> = arrayOf(migration, migration2to3, migration3to4)
}