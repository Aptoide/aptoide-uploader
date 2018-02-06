package pt.caixamagica.aptoide.uploader.util;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.ValidationException;
import pt.caixamagica.aptoide.uploader.AptoideUploaderApplication;
import pt.caixamagica.aptoide.uploader.webservices.json.OAuth;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 11-10-2017.
 */

public class StoredCredentialsManager {

  public static final String SHARED_PREFERENCES_FILE = "UploaderPrefs2";
  public static final byte[] SALT = new byte[] {
      -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -21, 77, -117, -36, -113, -11, 32, -64, 89
  };

  private final Context context;

  public StoredCredentialsManager(Context context) {
    this.context = context;
  }

  public void storeToken(UserCredentialsJson userCredentialsJson) {

    // Try to use more dgradleata here. ANDROID_ID is a single point of attack.
    String deviceId =
        Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

    AESObfuscator aesObfuscator = new AESObfuscator(SALT, context.getPackageName(), deviceId);

    SharedPreferences sharedpreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedpreferences.edit();

    editor.putString("token", aesObfuscator.obfuscate(userCredentialsJson.getToken(), "token"));
    editor.putString("refreshToken",
        aesObfuscator.obfuscate(userCredentialsJson.getRefreshToken(), "refreshToken"));
    editor.putString("repo", aesObfuscator.obfuscate(userCredentialsJson.getRepo(), "repo"));

    editor.commit();
  }

  public void storeToken(OAuth oAuth) {

    // Try to use more dgradleata here. ANDROID_ID is a single point of attack.
    String deviceId =
        Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

    AESObfuscator aesObfuscator = new AESObfuscator(SALT, context.getPackageName(), deviceId);

    SharedPreferences sharedpreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedpreferences.edit();

    editor.putString("token", aesObfuscator.obfuscate(oAuth.getAccess_token(), "token"));
    if (!TextUtils.isEmpty(oAuth.getRefreshToken())){
      editor.putString("refreshToken",
          aesObfuscator.obfuscate(oAuth.getRefreshToken(), "refreshToken"));
    }

    editor.commit();
  }

  public void storeRepo(UserCredentialsJson userCredentialsJson) {

    // Try to use more dgradleata here. ANDROID_ID is a single point of attack.
    String deviceId =
        Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

    AESObfuscator aesObfuscator = new AESObfuscator(SALT, context.getPackageName(), deviceId);

    SharedPreferences sharedpreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedpreferences.edit();

    editor.putString("repo", aesObfuscator.obfuscate(userCredentialsJson.getRepo(), "repo"));

    editor.commit();
  }

  public UserCredentialsJson getStoredUserCredentials() {

    AccountManager accountManager = AccountManager.get(context);

    SharedPreferences sharedpreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

    if (sharedpreferences != null
        && sharedpreferences.getAll()
        .size() > 0) {
      String deviceId =
          Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

      AESObfuscator aesObfuscator = new AESObfuscator(SALT, context.getPackageName(), deviceId);

      try {
        String token = aesObfuscator.unobfuscate(sharedpreferences.getString("token", ""), "token");
        String refreshToken =
            aesObfuscator.unobfuscate(sharedpreferences.getString("refreshToken", ""),
                "refreshToken");
        String repo = aesObfuscator.unobfuscate(sharedpreferences.getString("repo", ""), "repo");

        UserCredentialsJson userCredentialsJson = new UserCredentialsJson();
        userCredentialsJson.setToken(token);
        userCredentialsJson.setRefreshToken(refreshToken);
        userCredentialsJson.setRepo(repo);
        return userCredentialsJson;
      } catch (ValidationException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (!AptoideUploaderApplication.isForcedLogout()
        && accountManager.getAccountsByType("cm.aptoide.pt").length != 0) {

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

          UserCredentialsJson userCredentialsJson = new UserCredentialsJson();
          userCredentialsJson.setToken(c1.getString(c1.getColumnIndex("userToken")));
          userCredentialsJson.setRefreshToken(c2.getString(c2.getColumnIndex("userRefreshToken")));
          userCredentialsJson.setRepo(c3.getString(c3.getColumnIndex("userRepo")));

          storeToken(userCredentialsJson);

          c1.close();
          c2.close();
          c3.close();

          return userCredentialsJson;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }
}

