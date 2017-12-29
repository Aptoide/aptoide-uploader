package com.aptoide.uploader.apps.permission;

import android.util.Log;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by filipe on 29-12-2017.
 */

public class RequestAccessToStorageOnSubscribe implements ObservableOnSubscribe<Void> {

  private static final String TAG = RequestAccessToStorageOnSubscribe.class.getName();
  private final PermissionService permissionService;

  public RequestAccessToStorageOnSubscribe(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @Override public void subscribe(ObservableEmitter e) throws Exception {
    permissionService.requestAccessToStorage(() -> {
      if (!e.isDisposed()) {
        e.onComplete();
      }
    }, () -> {
      Log.d(TAG, "Permission denied to access ");
      e.onError(new SecurityException("Permission denied to access to external storage"));
    });
  }
}
