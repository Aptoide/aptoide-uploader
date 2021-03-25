package com.aptoide.uploader.apps;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

@Entity(tableName = "AutoUploadSelects") public class AutoUploadSelects {

  @NonNull @PrimaryKey @ColumnInfo(name = "packageName") private final String packageName;
  @ColumnInfo(name = "versionCode") private final int versionCode;
  @ColumnInfo(name = "isSelectedAutoUpload") private boolean isSelectedAutoUpload;

  public AutoUploadSelects(@NotNull String packageName, int versionCode,
      boolean isSelectedAutoUpload) {
    this.packageName = packageName;
    this.versionCode = versionCode;
    this.isSelectedAutoUpload = isSelectedAutoUpload;
  }

  public String getPackageName() {
    return packageName;
  }

  public int getVersionCode() {
    return versionCode;
  }

  public boolean isSelectedAutoUpload() {
    return isSelectedAutoUpload;
  }

  public void setSelectedAutoUpload(boolean selected) {
    isSelectedAutoUpload = selected;
  }
}
