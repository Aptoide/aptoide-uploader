package com.aptoide.uploader.apps.permission;

import android.Manifest;
import android.os.Build;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by filipe on 02-01-2018.
 */

public class UploadPermissionProvider {

  private final PermissionProvider permissionProvider;
  private final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;
  private final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
  private final PublishSubject<Boolean> storagePermissionSubject = PublishSubject.create();
  private final PublishSubject<Boolean> notificationPermissionSubject = PublishSubject.create();

  public UploadPermissionProvider(PermissionProvider permissionProvider) {
    this.permissionProvider = permissionProvider;
  }

  public void requestExternalStoragePermission() {
    // On Android 13+ (API 33+), READ_EXTERNAL_STORAGE is deprecated and not needed
    // for reading app APKs. Emit granted immediately.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      storagePermissionSubject.onNext(true);
      return;
    }
    permissionProvider.providePermissions(new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
  }

  public Observable<Boolean> permissionResultExternalStorage() {
    // On Android 13+ (API 33+), permission is not needed - listen to the subject
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return storagePermissionSubject.hide();
    }
    return permissionProvider.permissionResults(EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        .map(permissions -> permissions.get(0)
            .isGranted());
  }

  public void requestNotificationPermission() {
    // Notification permission is only required on Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionProvider.providePermissions(new String[] {
          Manifest.permission.POST_NOTIFICATIONS
      }, NOTIFICATION_PERMISSION_REQUEST_CODE);
    } else {
      // On older versions, notification permission is granted by default
      notificationPermissionSubject.onNext(true);
    }
  }

  public Observable<Boolean> permissionResultNotification() {
    // On Android < 13, notification permission is granted by default
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      return notificationPermissionSubject.hide();
    }
    return permissionProvider.permissionResults(NOTIFICATION_PERMISSION_REQUEST_CODE)
        .map(permissions -> permissions.get(0)
            .isGranted());
  }
}
