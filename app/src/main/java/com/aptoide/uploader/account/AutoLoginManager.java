package com.aptoide.uploader.account;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import com.aptoide.uploader.security.AESObfuscator;
import com.aptoide.uploader.security.ValidationException;
import io.reactivex.Single;

public class AutoLoginManager {
  private Context context;
  private AutoLoginPersistence persistence;
  private static final int REQUEST_CODE_ACCOUNT_PERMISSION = 42;

  private static final String SHARED_PREFERENCES_FILE = "UploaderPrefs2";
  private static final byte[] SALT = new byte[] {
      -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -21, 77, -117, -36, -113, -11, 32, -64, 89
  };

  public AutoLoginManager(Context context, AutoLoginPersistence persistence) {
    this.context = context;
    this.persistence = persistence;
  }

  public Single<AutoLoginCredentials> getStoredUserCredentials() {
    AccountManager accountManager = AccountManager.get(context);
    String deviceId =
        Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

    AESObfuscator aesObfuscator = new AESObfuscator(SALT, context.getPackageName(), deviceId);

    SharedPreferences sharedpreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

    if (sharedpreferences != null
        && sharedpreferences.getAll()
        .size() > 0) {

      try {
        String token = aesObfuscator.unobfuscate(sharedpreferences.getString("token", ""), "token");
        String refreshToken =
            aesObfuscator.unobfuscate(sharedpreferences.getString("refreshToken", ""),
                "refreshToken");

        AutoLoginCredentials autoLoginCredentials = new AutoLoginCredentials();
        autoLoginCredentials.setAccessToken(token);
        autoLoginCredentials.setRefreshToken(refreshToken);

        return Single.just(autoLoginCredentials);
      } catch (ValidationException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

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
