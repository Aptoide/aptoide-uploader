/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import com.facebook.AppEventsLogger;
import io.fabric.sdk.android.Fabric;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by neuro on 23-03-2015.
 */
public class AptoideUploaderApplication extends Application {

  public static boolean firstLaunchApagar = true;
  private static AptoideUploaderApplication context;
  @Getter @Setter private static boolean forcedLogout = false;
  @Getter @Setter private String username;

  public static Context getContext() {
    return context;
  }

  @Override public void onCreate() {
    super.onCreate();
    Fabric.with(this, new Crashlytics());
    context = this;
    AppEventsLogger.activateApp(this);
  }
}
