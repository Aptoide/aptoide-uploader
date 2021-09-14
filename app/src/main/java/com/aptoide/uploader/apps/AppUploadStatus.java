package com.aptoide.uploader.apps;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.aptoide.uploader.apps.persistence.StatusConverter;
import org.jetbrains.annotations.NotNull;

@Entity(tableName = "AppUploads") public class AppUploadStatus {

  @ColumnInfo(name = "md5") private final String md5;
  @NonNull @PrimaryKey @ColumnInfo(name = "packageName") private final String packageName;
  @ColumnInfo(name = "versionCode") private final int versionCode;
  @ColumnInfo(name = "status") @TypeConverters(StatusConverter.class) private Status status;

  public AppUploadStatus(String md5, @NotNull String packageName, Status status, int versionCode) {
    this.md5 = md5;
    this.packageName = packageName;
    this.status = status;
    this.versionCode = versionCode;
  }

  public String getMd5() {
    return md5;
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean isUploaded() {
    return status.equals(Status.IN_STORE);
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override public int hashCode() {
    int result = md5 != null ? md5.hashCode() : 0;
    result = 31 * result + packageName.hashCode();
    result = 31 * result + (getStringVersionCode() != null ? getStringVersionCode().hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AppUploadStatus that = (AppUploadStatus) o;

    if (md5 != null ? !md5.equals(that.md5) : that.md5 != null) return false;
    if (!packageName.equals(that.packageName)) return false;
    if (getStringVersionCode() != null ? !getStringVersionCode().equals(that.getStringVersionCode())
        : that.getStringVersionCode() != null) {
      return false;
    }
    return status == that.status;
  }

  public int getVersionCode() {
    return versionCode;
  }

  public String getStringVersionCode() {
    return String.valueOf(versionCode);
  }

  public enum Status {
    UNKNOWN(0), IN_STORE(1), NOT_IN_STORE(2), PROCESSING(3);

    private int code;

    Status(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }
}
