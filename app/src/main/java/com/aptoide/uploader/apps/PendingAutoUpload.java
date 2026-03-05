package com.aptoide.uploader.apps;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "PendingAutoUpload")
public class PendingAutoUpload {
  @PrimaryKey @NonNull private String packageName;
  private long createdTimestamp;

  public PendingAutoUpload(@NonNull String packageName, long createdTimestamp) {
    this.packageName = packageName;
    this.createdTimestamp = createdTimestamp;
  }

  @NonNull public String getPackageName() {
    return packageName;
  }

  public void setPackageName(@NonNull String packageName) {
    this.packageName = packageName;
  }

  public long getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(long createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }
}
