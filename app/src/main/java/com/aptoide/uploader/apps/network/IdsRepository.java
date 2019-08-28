package com.aptoide.uploader.apps.network;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import java.util.UUID;

/**
 * Created by neuro on 11-07-2016.
 */
public class IdsRepository {

  private static final String TAG = IdsRepository.class.getSimpleName();

  private static final String APTOIDE_CLIENT_UUID = "aptoide_client_uuid";
  private static final String ANDROID_ID_CLIENT = "androidId";

  private final SharedPreferences sharedPreferences;

  /**
   * Use the constructor were all the needed dependencies for this entity are injected.
   */
  public IdsRepository(SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  public synchronized String getUniqueIdentifier() {
    String aptoideId = sharedPreferences.getString(APTOIDE_CLIENT_UUID, null);
    if (!TextUtils.isEmpty(aptoideId)) {
      Log.v(TAG, "getUniqueIdentifier: in sharedPreferences: " + aptoideId);
      return aptoideId;
    }
    if (TextUtils.isEmpty(aptoideId)) {
      aptoideId = getAndroidId();
      if (!TextUtils.isEmpty(aptoideId)) {
        Log.v(TAG, "getUniqueIdentifier: getAndroidId: " + aptoideId);
      }
    } else {
      Log.v(TAG, "getUniqueIdentifier: getGoogleAdvertisingId: " + aptoideId);
    }

    if (TextUtils.isEmpty(aptoideId)) {
      aptoideId = UUID.randomUUID()
          .toString();
      Log.v(TAG, "getUniqueIdentifier: randomUUID: " + aptoideId);
    }

    sharedPreferences.edit()
        .putString(APTOIDE_CLIENT_UUID, aptoideId)
        .apply();
    return aptoideId;
  }

  public synchronized String getAndroidId() {
    String androidId = sharedPreferences.getString(ANDROID_ID_CLIENT, null);
    if (!TextUtils.isEmpty(androidId)) {
      return androidId;
    }
    if (sharedPreferences.getString(ANDROID_ID_CLIENT, null) != null) {
      throw new RuntimeException("Android ID already set!");
    }

    sharedPreferences.edit()
        .putString(ANDROID_ID_CLIENT, androidId)
        .apply();
    return androidId;
  }
}