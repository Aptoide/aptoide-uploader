package com.aptoide.uploader.apps.permission;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.Nullable;
import io.reactivex.functions.Action;

/**
 * Created by filipe on 29-12-2017.
 */

public interface PermissionService {

  @TargetApi(Build.VERSION_CODES.M) void requestAccessToStorage(
      @Nullable Action toRunWhenAccessIsGranted, @Nullable Action toRunWhenAccessIsDenied);
}
