package com.aptoide.uploader;

import android.content.Intent;
import android.preference.PreferenceManager;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.network.AccountResponseMapper;
import com.aptoide.uploader.account.network.RetrofitAccountService;
import com.aptoide.uploader.account.persistence.SharedPreferencesAccountPersistence;
import com.aptoide.uploader.apps.AccountStoreNameProvider;
import com.aptoide.uploader.apps.OkioMd5Calculator;
import com.aptoide.uploader.apps.PackageManagerInstalledAppsProvider;
import com.aptoide.uploader.apps.StoreManager;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.network.UploadService;
import com.aptoide.uploader.apps.persistence.MemoryUploaderPersistence;
import com.aptoide.uploader.security.SecurityAlgorithms;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.util.HashSet;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class UploaderApplication extends NotificationApplicationView {

  private AptoideAccountManager accountManager;
  private StoreManager storeManager;
  private UploadManager uploadManager;

  @Override public void onCreate() {
    super.onCreate();
    startService(new Intent(this, SyncUploadService.class));
    // TODO: 27-12-2017 filipe need to stop the service
  }

  public AptoideAccountManager getAccountManager() {
    if (accountManager == null) {
      final Retrofit retrofitV2 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://webservices.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      final Retrofit retrofitV3 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://webservices.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      final Retrofit retrofitV7 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://ws75.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      accountManager = new AptoideAccountManager(
          new RetrofitAccountService(retrofitV2.create(RetrofitAccountService.ServiceV2.class),
              retrofitV3.create(RetrofitAccountService.ServiceV3.class),
              retrofitV7.create(RetrofitAccountService.ServiceV7.class), new SecurityAlgorithms(),
              new AccountResponseMapper()),
          new SharedPreferencesAccountPersistence(PublishSubject.create(),
              PreferenceManager.getDefaultSharedPreferences(this), Schedulers.io()));
    }
    return accountManager;
  }

  public StoreManager getAppsManager() {
    if (storeManager == null) {
      storeManager = new StoreManager(new PackageManagerInstalledAppsProvider(getPackageManager()),
          new AccountStoreNameProvider(getAccountManager()), null, null);
    }
    return storeManager;
  }

  public UploadManager getUploadManager() {
    if (uploadManager == null) {

      final Retrofit retrofitV7 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://ws75.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      final Retrofit retrofitV3 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://webservices.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      uploadManager = new UploadManager(
          new UploadService(retrofitV7.create(UploadService.ServiceV7.class),
              retrofitV3.create(UploadService.ServiceV3.class)),
          new MemoryUploaderPersistence(new HashSet<>()), new OkioMd5Calculator());
    }
    return uploadManager;
  }
}
