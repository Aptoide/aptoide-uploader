package com.aptoide.uploader.apps.permission;

import android.Manifest;
import io.reactivex.Observable;

/**
 * Created by filipe on 02-01-2018.
 */

public class UploadPermissionProvider {

  private final PermissionProvider permissionProvider;
  private final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;

  public UploadPermissionProvider(PermissionProvider permissionProvider) {
    this.permissionProvider = permissionProvider;
  }

  public void requestExternalStoragePermission() {
    permissionProvider.providePermissions(new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
  }

  public Observable<Boolean> permissionResultExternalStorage() {
    return permissionProvider.permissionResults(EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        .map(permissions -> permissions.get(0)
            .isGranted());
  }
}
