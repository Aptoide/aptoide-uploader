package com.aptoide.uploader;

import android.accounts.AccountManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.CredentialsValidator;
import com.aptoide.uploader.account.VanillaLoginProvider;
import com.aptoide.uploader.account.network.AccountResponseMapper;
import com.aptoide.uploader.account.network.RetrofitAccountService;
import com.aptoide.uploader.account.persistence.SharedPreferencesAccountPersistence;
import com.aptoide.uploader.apps.AccountStoreNameProvider;
import com.aptoide.uploader.apps.AndroidLanguageManager;
import com.aptoide.uploader.apps.LanguageManager;
import com.aptoide.uploader.apps.OkioMd5Calculator;
import com.aptoide.uploader.apps.PackageManagerInstalledAppsProvider;
import com.aptoide.uploader.apps.StoreManager;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.network.RetrofitUploadService;
import com.aptoide.uploader.apps.persistence.MemoryUploaderPersistence;
import com.aptoide.uploader.security.AptoideAccessTokenProvider;
import com.aptoide.uploader.security.AuthenticationPersistance;
import com.aptoide.uploader.security.AuthenticationProvider;
import com.aptoide.uploader.security.SecurityAlgorithms;
import com.aptoide.uploader.security.SharedPreferencesAuthenticationPersistence;
import com.aptoide.uploader.security.VanillaContentProviderParser;
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
  private LanguageManager languageManager;
  private AuthenticationProvider authenticationProvider;
  private FirstLaunchPersistence firstLaunchPersistence;

  @Override public void onCreate() {
    super.onCreate();

    startService(new Intent(this, SyncUploadService.class));
    // TODO: 27-12-2017 filipe need to stop the service

  }

  public AptoideAccountManager getAccountManager() {
    if (accountManager == null) {
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

      final SharedPreferencesAccountPersistence accountPersistence =
          new SharedPreferencesAccountPersistence(PublishSubject.create(),
              PreferenceManager.getDefaultSharedPreferences(this), Schedulers.io());

      accountManager = new AptoideAccountManager(
          new RetrofitAccountService(retrofitV3.create(RetrofitAccountService.ServiceV3.class),
              retrofitV7.create(RetrofitAccountService.ServiceV7.class), new SecurityAlgorithms(),
              new AccountResponseMapper(), getAuthenticationProvider()), accountPersistence,
          new CredentialsValidator(), new VanillaLoginProvider(accountPersistence,
          new SharedPreferencesAuthenticationPersistence(
              PreferenceManager.getDefaultSharedPreferences(this)),
          new VanillaContentProviderParser(AccountManager.get(this), this.getContentResolver())),
          getFirstLaunchPersistence());
    }
    return accountManager;
  }

  private FirstLaunchPersistence getFirstLaunchPersistence() {
    if (firstLaunchPersistence == null) {
      firstLaunchPersistence = new SharedPreferencesFirstLaunchPersistence(
          PreferenceManager.getDefaultSharedPreferences(this));
    }
    return firstLaunchPersistence;
  }

  public AuthenticationProvider getAuthenticationProvider() {
    if (authenticationProvider == null) {
      final Retrofit retrofitV3 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://webservices.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      final AuthenticationPersistance authenticationPersistance =
          new SharedPreferencesAuthenticationPersistence(
              PreferenceManager.getDefaultSharedPreferences(this));

      authenticationProvider = new AptoideAccessTokenProvider(authenticationPersistance,
          retrofitV3.create(AptoideAccessTokenProvider.ServiceV3.class));
    }
    return authenticationProvider;
  }

  public StoreManager getAppsManager() {
    if (storeManager == null) {
      storeManager = new StoreManager(new PackageManagerInstalledAppsProvider(getPackageManager()),
          new AccountStoreNameProvider(getAccountManager()), getUploadManager(),
          getLanguageManager(), getAccountManager());
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

      final Retrofit retrofitV7Secondary = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://ws75-secondary.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      final Retrofit retrofitV3 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(new OkHttpClient())
          .baseUrl("http://webservices.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      uploadManager = new UploadManager(
          new RetrofitUploadService(retrofitV7.create(RetrofitUploadService.ServiceV7.class),
              retrofitV7Secondary.create(RetrofitUploadService.ServiceV7Secondary.class),
              retrofitV3.create(RetrofitUploadService.ServiceV3.class),
              getAuthenticationProvider()), new MemoryUploaderPersistence(new HashSet<>()),
          new OkioMd5Calculator());
    }
    return uploadManager;
  }

  public LanguageManager getLanguageManager() {
    if (languageManager == null) {
      languageManager = new AndroidLanguageManager();
    }
    return languageManager;
  }
}
