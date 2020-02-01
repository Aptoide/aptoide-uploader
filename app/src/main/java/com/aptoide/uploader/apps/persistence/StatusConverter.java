package com.aptoide.uploader.apps.persistence;

import androidx.room.TypeConverter;
import com.aptoide.uploader.apps.AppUploadStatus;

import static com.aptoide.uploader.apps.AppUploadStatus.Status.IN_STORE;
import static com.aptoide.uploader.apps.AppUploadStatus.Status.NOT_IN_STORE;
import static com.aptoide.uploader.apps.AppUploadStatus.Status.PROCESSING;
import static com.aptoide.uploader.apps.AppUploadStatus.Status.UNKNOWN;

public class StatusConverter {

  @TypeConverter public static AppUploadStatus.Status toStatus(int status) {
    if (status == UNKNOWN.getCode()) {
      return UNKNOWN;
    } else if (status == IN_STORE.getCode()) {
      return IN_STORE;
    } else if (status == NOT_IN_STORE.getCode()) {
      return NOT_IN_STORE;
    } else if (status == PROCESSING.getCode()) {
      return PROCESSING;
    } else {
      throw new IllegalArgumentException("Could not recognize status");
    }
  }

  @TypeConverter public static Integer toInteger(AppUploadStatus.Status status) {
    return status.getCode();
  }
}
