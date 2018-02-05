package com.aptoide.uploader.account.permission;

import android.Manifest;
import com.aptoide.uploader.apps.permission.PermissionProvider;
import io.reactivex.Observable;

/**
 * Created by filipe on 10-01-2018.
 */

public class AccountPermissionProvider {

  private PermissionProvider permissionProvider;
  private final int GET_ACCOUNTS_PERMISSION_REQUEST_CODE = 2;

  public AccountPermissionProvider(PermissionProvider permissionProvider) {
    this.permissionProvider = permissionProvider;
  }

  public void requestGetAccountPermission() {
    permissionProvider.providePermissions(new String[] {
        Manifest.permission.GET_ACCOUNTS
    }, GET_ACCOUNTS_PERMISSION_REQUEST_CODE);
  }

  public Observable<Boolean> permissionResultGetAccounts() {
    return permissionProvider.permissionResults(GET_ACCOUNTS_PERMISSION_REQUEST_CODE)
        .map(permissions -> permissions.get(0)
            .isGranted());
  }
}
