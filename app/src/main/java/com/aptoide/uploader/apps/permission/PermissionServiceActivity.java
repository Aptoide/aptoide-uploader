package com.aptoide.uploader.apps.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import io.reactivex.functions.Action;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filipe on 29-12-2017.
 */

public abstract class PermissionServiceActivity extends AppCompatActivity
    implements PermissionService {

  private static final String TAG = PermissionServiceActivity.class.getSimpleName();
  private static final int EXTERNAL_STORAGE_REQUEST_CODE = 1;
  @Nullable private Action toRunWhenAccessToExternalStorageIsGranted;
  @Nullable private Action toRunWhenAccessToExternalStorageIsDenied;

  @TargetApi(Build.VERSION_CODES.M) @Override
  public void requestAccessToStorage(@Nullable Action toRunWhenAccessIsGranted,
      @Nullable Action toRunWhenAccessIsDenied) {

    List<String> notGrantedPermissions = new ArrayList<>();
    String[] permissions = new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission)
          != PackageManager.PERMISSION_GRANTED) {
        notGrantedPermissions.add(permission);
      }
    }
    if (notGrantedPermissions.isEmpty()) {
      if (toRunWhenAccessIsGranted != null) {
        try {
          toRunWhenAccessIsGranted.run();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else {
      this.toRunWhenAccessToExternalStorageIsGranted = toRunWhenAccessIsGranted;
      this.toRunWhenAccessToExternalStorageIsDenied = toRunWhenAccessIsDenied;

      ActivityCompat.requestPermissions(this, notGrantedPermissions.toArray(new String[0]),
          EXTERNAL_STORAGE_REQUEST_CODE);
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {

    switch (requestCode) {
      case EXTERNAL_STORAGE_REQUEST_CODE:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Log.v(TAG, "access to read and write to external storage was granted");
          if (toRunWhenAccessToExternalStorageIsGranted != null) {
            try {
              toRunWhenAccessToExternalStorageIsGranted.run();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        } else {
          if (toRunWhenAccessToExternalStorageIsDenied != null) {
            try {
              toRunWhenAccessToExternalStorageIsDenied.run();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        break;
    }
  }
}
