package com.aptoide.uploader.account;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import io.reactivex.Single;

public class AutoLoginManager {
  private static Context context;
  private AutoLoginCredentials autoLoginCredentials = new AutoLoginCredentials();
  private static AutoLoginManager single_instance;

  public AutoLoginManager(Context context) {
    this.context = context;
  }

  public static AutoLoginManager getInstance(Context context) {
    if (single_instance == null) single_instance = new AutoLoginManager(context);
    return single_instance;
  }

  public AutoLoginCredentials getAutoLoginCredentials() {
    return autoLoginCredentials;
  }

  public Single<AutoLoginCredentials> getStoredUserCredentials() {
    return Single.just(autoLoginCredentials);
  }

  public Single<AutoLoginCredentials> getFirstStoredUserCredentials() {
    try {
      String URL = "content://cm.aptoide.pt.StubProvider";
      Uri token_uri = Uri.parse(URL + "/token");
      Uri refresh_token_uri = Uri.parse(URL + "/refreshToken");
      Uri repo_uri = Uri.parse(URL + "/repo");
      Uri email_uri = Uri.parse(URL + "/loginName");
      Uri nickname_uri = Uri.parse(URL + "/loginNickname");
      Uri avatar_uri = Uri.parse(URL + "/loginAvatar");

      Cursor c1 = context.getContentResolver()
          .query(token_uri, null, null, null, null);
      Cursor c2 = context.getContentResolver()
          .query(refresh_token_uri, null, null, null, null);
      Cursor c3 = context.getContentResolver()
          .query(repo_uri, null, null, null, null);
      Cursor c4 = context.getContentResolver()
          .query(email_uri, null, null, null, null);
      Cursor c5 = context.getContentResolver()
          .query(nickname_uri, null, null, null, null);
      Cursor c6 = context.getContentResolver()
          .query(avatar_uri, null, null, null, null);

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
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Single.error(e);
    }
    return Single.just(autoLoginCredentials);
  }

  public boolean isNullOrEmpty(String str) {
    return str == null || str.trim()
        .isEmpty();
  }

  public void checkAvatar(String name, Navigator nav) {
    if (isNullOrEmpty(getAutoLoginCredentials().getAvatarPath())) {
      nav.navigateToAutoLoginFragment(name);
    } else {
      nav.navigateToAutoLoginFragment(name, getAutoLoginCredentials().getAvatarPath());
    }
  }

  public void checkAvailableFieldsAndNavigateTo(Navigator nav) {
    if (isNullOrEmpty(getAutoLoginCredentials().getAccessToken())) {
      nav.navigateToLoginFragment();
    } else {
      if (isNullOrEmpty(getAutoLoginCredentials().getStoreName())) {
        if (isNullOrEmpty(getAutoLoginCredentials().getName())) {
          checkAvatar(getAutoLoginCredentials().getEmail(), nav);
        } else {
          checkAvatar(getAutoLoginCredentials().getName(), nav);
        }
      } else {
        checkAvatar(getAutoLoginCredentials().getStoreName(), nav);
      }
    }
  }
}
