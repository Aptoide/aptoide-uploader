package com.aptoide.uploader.apps;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

@Entity(tableName = "AutoUploadSelects") public class AutoUploadSelects {

  @NonNull @PrimaryKey @ColumnInfo(name = "packageName") private final String packageName;
  @ColumnInfo(name = "isSelectedAutoUpload") private boolean isSelectedAutoUpload;

  public AutoUploadSelects(@NotNull String packageName, boolean isSelectedAutoUpload) {
    this.packageName = packageName;
    this.isSelectedAutoUpload = isSelectedAutoUpload;
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean isSelectedAutoUpload() {
    return isSelectedAutoUpload;
  }

  public void setSelectedAutoUpload(boolean selected) {
    isSelectedAutoUpload = selected;
  }
}
