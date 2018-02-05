package com.aptoide.uploader.security;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by filipe on 08-01-2018.
 */

public class VanillaContentProviderParser {

  private final String CONTENT_PROVIDER_URL = "content://cm.aptoide.pt.StubProvider";
  private final String APTOIDE_VANILLA_PACKAGE_NAME = "cm.aptoide.pt";
  private final AccountManager accountManager;
  private final ContentResolver contentResolver;

  public VanillaContentProviderParser(AccountManager accountManager,
      ContentResolver contentResolver) {
    this.accountManager = accountManager;
    this.contentResolver = contentResolver;
  }

  public String getStoreName() {
    String storeName = "";
    if (accountManager.getAccountsByType(APTOIDE_VANILLA_PACKAGE_NAME).length > 0) {
      Uri storeNameUri = Uri.parse(CONTENT_PROVIDER_URL + "/repo");
      try {
        Cursor cursor = contentResolver.query(storeNameUri, null, null, null, null);
        if (cursor != null) {
          cursor.moveToFirst();
          storeName = cursor.getString(cursor.getColumnIndex("userRepo"));
          cursor.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return storeName;
  }

  public String getRefreshToken() {
    String refreshToken = "";
    if (accountManager.getAccountsByType(APTOIDE_VANILLA_PACKAGE_NAME).length > 0) {
      Uri refreshTokenUri = Uri.parse(CONTENT_PROVIDER_URL + "/refreshToken");
      try {
        Cursor cursor = contentResolver.query(refreshTokenUri, null, null, null, null);
        if (cursor != null) {
          cursor.moveToFirst();
          refreshToken = cursor.getString(cursor.getColumnIndex("userRefreshToken"));
          cursor.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return refreshToken;
  }

  public String getAccessToken() {
    String accessToken = "";
    if (accountManager.getAccountsByType(APTOIDE_VANILLA_PACKAGE_NAME).length > 0) {
      Uri accessTokenUri = Uri.parse(CONTENT_PROVIDER_URL + "/token");
      try {
        Cursor cursor = contentResolver.query(accessTokenUri, null, null, null, null);
        if (cursor != null) {
          cursor.moveToFirst();
          accessToken = cursor.getString(cursor.getColumnIndex("userToken"));
          cursor.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return accessToken;
  }
}
