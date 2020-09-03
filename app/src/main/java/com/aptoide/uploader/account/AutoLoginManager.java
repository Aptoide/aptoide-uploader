package com.aptoide.uploader.account;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import io.reactivex.Single;

public class AutoLoginManager {
  private static Context context;
  private AutoLoginCredentials autoLoginCredentials = new AutoLoginCredentials();
  private static AutoLoginManager instance;

  public AutoLoginManager(Context context) {
    AutoLoginManager.context = context.getApplicationContext();
  }

  public static AutoLoginManager getInstance(Context context) {
    if (instance == null) instance = new AutoLoginManager(context);
    return instance;
  }

  public AutoLoginCredentials getAutoLoginCredentials() {
    return autoLoginCredentials;
  }

  public Single<AutoLoginCredentials> fetchStoredUserCredentials() {
    if (autoLoginCredentials.getAccessToken() == null || autoLoginCredentials.getAccessToken()
        .trim()
        .isEmpty()) {
      try {
        String URL = "content://cm.aptoide.pt.StubProvider";
        Uri tokenUri = Uri.parse(URL + "/token");
        Uri refreshTokenUri = Uri.parse(URL + "/refreshToken");
        Uri storeNameUri = Uri.parse(URL + "/repo");
        Uri emailUri = Uri.parse(URL + "/loginName");

        Cursor c1 = context.getContentResolver()
            .query(tokenUri, null, null, null, null);
        Cursor c2 = context.getContentResolver()
            .query(refreshTokenUri, null, null, null, null);
        Cursor c3 = context.getContentResolver()
            .query(storeNameUri, null, null, null, null);
        Cursor c4 = context.getContentResolver()
            .query(emailUri, null, null, null, null);

        if (c1 != null && c2 != null) {
          c1.moveToFirst();
          c2.moveToFirst();
          autoLoginCredentials.setAccessToken(c1.getString(c1.getColumnIndex("userToken")));
          autoLoginCredentials.setRefreshToken(c2.getString(c2.getColumnIndex("userRefreshToken")));
          c1.close();
          c2.close();

          if (c3 != null) {
            c3.moveToFirst();
            autoLoginCredentials.setStoreName(c3.getString(c3.getColumnIndex("userRepo")));
            c3.close();
          }

          if (c4 != null) {
            c4.moveToFirst();
            autoLoginCredentials.setEmail(c4.getString(c4.getColumnIndex("loginName")));
            c4.close();
          }
          try {
            Uri nameUri = Uri.parse(URL + "/loginNickname");
            Uri avatarUri = Uri.parse(URL + "/loginAvatar");

            Cursor c5 = context.getContentResolver()
                .query(nameUri, null, null, null, null);
            Cursor c6 = context.getContentResolver()
                .query(avatarUri, null, null, null, null);

            if (c5 != null) {
              c5.moveToFirst();
              autoLoginCredentials.setName(c5.getString(c5.getColumnIndex("loginNickname")));
              c5.close();
            }
            if (c6 != null) {
              c6.moveToFirst();
              autoLoginCredentials.setAvatarPath(c6.getString(c6.getColumnIndex("loginAvatar")));
              c6.close();
            }
          } catch (Exception e) {
            e.printStackTrace();
            return Single.error(e);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        return Single.error(e);
      }
    }
    return Single.just(autoLoginCredentials);
  }

  private void checkAvatarAndNavigateTo(String name, Navigator nav) {
    if (autoLoginCredentials.getAvatarPath() == null || autoLoginCredentials.getAvatarPath()
        .trim()
        .isEmpty()) {
      nav.navigateToAutoLoginFragment(name);
    } else {
      nav.navigateToAutoLoginFragment(name, autoLoginCredentials.getAvatarPath());
    }
  }

  public void checkAvailableFieldsAndNavigateTo(Navigator nav) {
    if (autoLoginCredentials.getAccessToken() == null || autoLoginCredentials.getAccessToken()
        .trim()
        .isEmpty()) {
      nav.navigateToLoginFragment();
    } else {
      if (autoLoginCredentials.getStoreName() == null || autoLoginCredentials.getStoreName()
          .trim()
          .isEmpty()) {
        if (autoLoginCredentials.getName() == null || autoLoginCredentials.getName()
            .trim()
            .isEmpty()) {
          checkAvatarAndNavigateTo(autoLoginCredentials.getEmail(), nav);
        } else {
          checkAvatarAndNavigateTo(autoLoginCredentials.getName(), nav);
        }
      } else {
        checkAvatarAndNavigateTo(autoLoginCredentials.getStoreName(), nav);
      }
    }
  }
}
