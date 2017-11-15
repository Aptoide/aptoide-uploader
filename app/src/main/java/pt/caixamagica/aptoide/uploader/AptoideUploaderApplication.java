/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import com.crashlytics.android.Crashlytics;
import com.facebook.AppEventsLogger;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import pt.caixamagica.aptoide.uploader.retrofit.RetrofitSpiceServiceUploaderSecondary;
import pt.caixamagica.aptoide.uploader.retrofit.request.UploadedAppsRequest;
import pt.caixamagica.aptoide.uploader.util.InstalledUtils;
import pt.caixamagica.aptoide.uploader.util.Md5AsyncUtils;
import pt.caixamagica.aptoide.uploader.util.StoredCredentialsManager;
import pt.caixamagica.aptoide.uploader.util.StoredUploadedAppsManager;
import pt.caixamagica.aptoide.uploader.webservices.json.UploadedAppsJson;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 23-03-2015.
 */
public class AptoideUploaderApplication extends Application {

  public static boolean firstLaunchApagar = true;
  private static AptoideUploaderApplication context;
  @Getter @Setter private static boolean forcedLogout = false;
  @Getter @Setter private String username;
  private double BUFFER_SIZE = 5;
  private SpiceManager spiceManager;
  private StoredCredentialsManager storedCredentialsManager;
  private List<UploadedApp> listOfAppsInStore;
  private StoredUploadedAppsManager storedUploadedAppsManager;
  public static final String SHARED_PREFERENCES_FILE = "UploaderPrefs2";

  public static Context getContext() {
    return context;
  }

  @Override public void onCreate() {
    super.onCreate();
    Fabric.with(this, new Crashlytics());
    context = this;
    AppEventsLogger.activateApp(this);

    if (BuildConfig.DEBUG) {
      MultiDex.install(this);
    }

    storedCredentialsManager = new StoredCredentialsManager(this.getApplicationContext());

    storedUploadedAppsManager = new StoredUploadedAppsManager(this.getApplicationContext()
        .getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE));

    listOfAppsInStore = new ArrayList<>();

    if (isUserLoggedIn()) {
      spiceManager = new SpiceManager(RetrofitSpiceServiceUploaderSecondary.class);
      spiceManager.start(this);
      refreshInstalledAppsMd5List();
    }
  }

  private void refreshInstalledAppsMd5List() {
    InstalledUtils installedUtils = new InstalledUtils(this, storedUploadedAppsManager);
    Md5AsyncUtils md5AsyncUtils = new Md5AsyncUtils(this, installedUtils);

    List<SelectablePackageInfo> selectablePackageInfos = installedUtils.nonSystemPackages(false);
    md5AsyncUtils.computeMd5(selectablePackageInfos,
        createMd5Listener(selectablePackageInfos.size()));
  }

  private Md5AsyncUtils.OnNewUploadedApps createMd5Listener(final int size) {
    return new Md5AsyncUtils.OnNewUploadedApps() {
      private AtomicInteger atomicInteger = new AtomicInteger(0);
      private ConcurrentLinkedQueue<Md5AsyncUtils.Model> concurrentLinkedQueue =
          new ConcurrentLinkedQueue<>();

      @Override public void onNewUploadedApps(Md5AsyncUtils.Model model) {
        int count = atomicInteger.incrementAndGet();
        if (count % BUFFER_SIZE == 0 || count == size) {
          List<Md5AsyncUtils.Model> modelList = createModelList();
          sendUploadedAppsRequest(modelList);
        } else {
          concurrentLinkedQueue.add(model);
        }
      }

      private List<Md5AsyncUtils.Model> createModelList() {
        List<Md5AsyncUtils.Model> list = new LinkedList<>();

        for (int i = 0; i < Math.min(BUFFER_SIZE, concurrentLinkedQueue.size()); i++) {
          if (!concurrentLinkedQueue.isEmpty()) {
            list.add(concurrentLinkedQueue.poll());
          }
        }

        return list;
      }

      private void sendUploadedAppsRequest(List<Md5AsyncUtils.Model> modelList) {
        UserCredentialsJson storedUserCredentials =
            new StoredCredentialsManager(getContext()).getStoredUserCredentials();

        String token = storedUserCredentials.getToken();
        String storeName = storedUserCredentials.getRepo();

        spiceManager.execute(
            new UploadedAppsRequest(token, storeName, createMd5StringList(modelList)),
            new RequestListener<UploadedAppsJson>() {
              @Override public void onRequestFailure(SpiceException spiceException) {
                spiceException.printStackTrace();
              }

              @Override public void onRequestSuccess(UploadedAppsJson uploadedAppsJson) {
                System.out.println("request was successful");

                List<UploadedAppsJson.DataList.App> remoteAppsList =
                    uploadedAppsJson.datalist.getList();

                for (int i = 0; i < uploadedAppsJson.datalist.getList()
                    .size(); i++) {
                  listOfAppsInStore.add(new UploadedApp(remoteAppsList.get(i)
                      .getFile()
                      .getPackageName()
                      .getName(), remoteAppsList.get(i)
                      .getFile()
                      .getVercode()));
                }
                updateStoredUploadedApps(listOfAppsInStore);
              }
            });
      }
    };
  }

  private void updateStoredUploadedApps(List<UploadedApp> listOfAppsInStore) {
    storedUploadedAppsManager.saveUploadedApps(listOfAppsInStore);
  }

  private List<String> createMd5StringList(List<Md5AsyncUtils.Model> modelList) {
    String str = "[";

    for (Md5AsyncUtils.Model model : modelList) {
      str += model.getMd5sum() + ",";
    }

    char[] chars = str.toCharArray();
    chars[str.length() - 1] = ']';

    List<String> strings = new LinkedList<>();
    for (Md5AsyncUtils.Model model : modelList) {
      strings.add(model.getMd5sum());
    }

    return strings;
  }

  private boolean isUserLoggedIn() {
    return storedCredentialsManager.getStoredUserCredentials() != null;
  }
}
