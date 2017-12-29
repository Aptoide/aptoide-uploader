package com.aptoide.uploader.apps.permission;

import io.reactivex.Observable;

/**
 * Created by filipe on 29-12-2017.
 */

public class PermissionManager {



  public Observable<Void> requestExternalStoragePermission(PermissionService permissionService) {
    return Observable.create(new RequestAccessToStorageOnSubscribe(permissionService));
  }
}
