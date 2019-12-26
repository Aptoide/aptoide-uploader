package com.aptoide.uploader.account;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import io.reactivex.Single;

public class AutoLoginManager {
  private Context context;
  private AutoLoginPersistence persistence;

  public AutoLoginManager(Context context, AutoLoginPersistence persistence) {
    this.context = context;
    this.persistence = persistence;
  }

  public Single<AutoLoginCredentials> getStoredUserCredentials() {
    try {
      String URL = "content://cm.aptoide.pt.StubProvider";
      Uri token_uri = Uri.parse(URL + "/token");
      Uri refresh_token_uri = Uri.parse(URL + "/refreshToken");
      Uri repo_uri = Uri.parse(URL + "/repo");

      Cursor c1 = context.getContentResolver()
          .query(token_uri, null, null, null, null);
      Cursor c2 = context.getContentResolver()
          .query(refresh_token_uri, null, null, null, null);
      Cursor c3 = context.getContentResolver()
          .query(repo_uri, null, null, null, null);

      if (c1 != null && c2 != null && c3 != null) {

        c1.moveToFirst();
        c2.moveToFirst();
        c3.moveToFirst();

        AutoLoginCredentials autoLoginCredentials = new AutoLoginCredentials();
        autoLoginCredentials.setAccessToken(c1.getString(c1.getColumnIndex("userToken")));
        autoLoginCredentials.setRefreshToken(c2.getString(c2.getColumnIndex("userRefreshToken")));

        c1.close();
        c2.close();
        c3.close();

        return Single.just(autoLoginCredentials);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Single.error(e);
    }
    return Single.just(new AutoLoginCredentials());
  }

  public boolean getAutologinFlag() {
    return persistence.isForcedLogout();
  }

  public void setAutoLoginFlag(boolean flag) {
    persistence.setForcedLogout(flag);
  }
}
