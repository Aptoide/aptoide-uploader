/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.octo.android.robospice.SpiceManager;
import io.fabric.sdk.android.Fabric;
import lombok.Getter;
import lombok.Setter;
import pt.caixamagica.aptoide.uploader.retrofit.RetrofitSpiceServiceUploaderSecondary;
import pt.caixamagica.aptoide.uploader.util.AppsInStorePersister;
import pt.caixamagica.aptoide.uploader.util.InstalledUtils;
import pt.caixamagica.aptoide.uploader.util.Md5AsyncUtils;
import pt.caixamagica.aptoide.uploader.util.StoredCredentialsManager;

/**
 * Created by neuro on 23-03-2015.
 */
public class AptoideUploaderApplication extends Application {

  public static final String APPS_IN_MY_STORE_SHARED_PREFERENCES_FILE = "AppsInMyStore";
  public static boolean firstLaunchApagar = true;
  private static AptoideUploaderApplication context;
  @Getter @Setter private static boolean forcedLogout = false;
  @Getter @Setter private String username;
  private StoredCredentialsManager storedCredentialsManager;
  private AppsInStoreController appsInStoreController;
  private CallbackManager callbackManager;

  public static Context getContext() {
    return context;
  }

  @Override public void onCreate() {
    super.onCreate();
    Fabric.with(this, new Crashlytics());
    context = this;
    FacebookSdk.sdkInitialize(getApplicationContext());

    if (BuildConfig.DEBUG) {
      MultiDex.install(this);
    }

    storedCredentialsManager = new StoredCredentialsManager(this.getApplicationContext());

    if (hasStore()) {
      getAppsInStoreController().start();
    }
    callbackManager = CallbackManager.Factory.create();
  }

  private boolean hasStore() {
    return storedCredentialsManager.getStoredUserCredentials() != null && !TextUtils.isEmpty(
        storedCredentialsManager.getStoredUserCredentials()
            .getRepo());
  }

  public AppsInStoreController getAppsInStoreController() {
    if (appsInStoreController == null) {
      AppsInStorePersister appsInStorePersister = new AppsInStorePersister(
          this.getApplicationContext()
              .getSharedPreferences(APPS_IN_MY_STORE_SHARED_PREFERENCES_FILE,
                  Context.MODE_PRIVATE));

      appsInStoreController =
          new AppsInStoreController(new SpiceManager(RetrofitSpiceServiceUploaderSecondary.class),
              appsInStorePersister, new InstalledUtils(this, appsInStorePersister),
              new Md5AsyncUtils(this), getApplicationContext());
    }
    return appsInStoreController;
  }

  public CallbackManager getCallbackManager() {
    return callbackManager;
  }
}
