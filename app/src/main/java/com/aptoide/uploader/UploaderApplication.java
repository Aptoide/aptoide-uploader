package com.aptoide.uploader;

import android.content.Context;
import android.preference.PreferenceManager;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.account.AutoLoginManager;
import com.aptoide.uploader.account.CredentialsValidator;
import com.aptoide.uploader.account.SocialLogoutManager;
import com.aptoide.uploader.account.network.AccountResponseMapper;
import com.aptoide.uploader.account.network.RetrofitAccountService;
import com.aptoide.uploader.account.persistence.SharedPreferencesAccountPersistence;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.AccountStoreNameProvider;
import com.aptoide.uploader.apps.AndroidLanguageManager;
import com.aptoide.uploader.apps.AppUploadStatusManager;
import com.aptoide.uploader.apps.CategoriesManager;
import com.aptoide.uploader.apps.LanguageManager;
import com.aptoide.uploader.apps.OkioMd5Calculator;
import com.aptoide.uploader.apps.PackageManagerInstalledAppsProvider;
import com.aptoide.uploader.apps.ServiceBackgroundService;
import com.aptoide.uploader.apps.StoreManager;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.UploadProgressManager;
import com.aptoide.uploader.apps.network.AptoideConnectivityProvider;
import com.aptoide.uploader.apps.network.ConnectivityInterceptor;
import com.aptoide.uploader.apps.network.IdsRepository;
import com.aptoide.uploader.apps.network.RetrofitAppsUploadStatusService;
import com.aptoide.uploader.apps.network.RetrofitCategoriesService;
import com.aptoide.uploader.apps.network.RetrofitStoreService;
import com.aptoide.uploader.apps.network.RetrofitUploadService;
import com.aptoide.uploader.apps.network.TokenRevalidationInterceptorV3;
import com.aptoide.uploader.apps.network.TokenRevalidationInterceptorV7;
import com.aptoide.uploader.apps.network.UserAgentInterceptor;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.apps.persistence.MemoryAppUploadStatusPersistence;
import com.aptoide.uploader.apps.persistence.MemoryUploaderPersistence;
import com.aptoide.uploader.apps.persistence.UploaderPersistence;
import com.aptoide.uploader.security.AptoideAccessTokenProvider;
import com.aptoide.uploader.security.AuthenticationPersistance;
import com.aptoide.uploader.security.AuthenticationProvider;
import com.aptoide.uploader.security.SecurityAlgorithms;
import com.aptoide.uploader.security.SharedPreferencesAuthenticationPersistence;
import com.aptoide.uploader.upload.AptoideAccountProvider;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
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
  private CategoriesManager categoriesManager;
  private OkioMd5Calculator md5Calculator;
  private UploaderPersistence uploadPersistence;
  private AppUploadStatusPersistence appUploadStatusPersistence;
  private AppUploadStatusManager appUploadStatusManager;
  private UploaderAnalytics uploaderAnalytics;
  private CallbackManager callbackManager;
  private static boolean forcedLogout = false;


  @Override public void onCreate() {
    super.onCreate();
    startFlurryAgent();
    callbackManager = CallbackManager.Factory.create();
    getUploadManager().start();
  }

  public UploadManager getUploadManager() {
    if (uploadManager == null) {
      OkHttpClient.Builder okhttpBuilder =
          new OkHttpClient.Builder().writeTimeout(30, TimeUnit.SECONDS)
              .readTimeout(30, TimeUnit.SECONDS)
              .connectTimeout(60, TimeUnit.SECONDS)
              .addInterceptor(getConnectivityInterceptor())
              .addInterceptor(getTokenRevalidationInterceptorV3())
              .addInterceptor(getUserAgentInterceptor());

      final Retrofit retrofitV3 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .baseUrl("http://upload.webservices.aptoide.com/webservices/")
          .addConverterFactory(MoshiConverterFactory.create())
          .client(okhttpBuilder.build())
          .build();

      UploadProgressManager uploadProgressManager = new UploadProgressManager();

      uploadManager = new UploadManager(
          new RetrofitUploadService(retrofitV3.create(RetrofitUploadService.ServiceV3.class),
              getAccessTokenProvider(), RetrofitUploadService.UploadType.APTOIDE_UPLOADER,
              uploadProgressManager, getUploaderAnalytics()), getUploadPersistence(),
          getMd5Calculator(), new ServiceBackgroundService(this, UploaderService.class),
          getAccessTokenProvider(), getAppUploadStatusManager(), getAppUploadStatusPersistence(),
          uploadProgressManager);
    }
    return uploadManager;
  }

  public IdsRepository getIdsRepository() {
    return new IdsRepository(PreferenceManager.getDefaultSharedPreferences(this));
  }

  public TokenRevalidationInterceptorV3 getTokenRevalidationInterceptorV3() {
    return new TokenRevalidationInterceptorV3(getAccessTokenProvider());
  }

  public TokenRevalidationInterceptorV7 getTokenRevalidationInterceptorV7() {
    return new TokenRevalidationInterceptorV7(getAccessTokenProvider());
  }

  public UserAgentInterceptor getUserAgentInterceptor() {
    return new UserAgentInterceptor(getIdsRepository());
  }

  public ConnectivityInterceptor getConnectivityInterceptor() {
    return new ConnectivityInterceptor(getApplicationContext());
  }

  public AptoideAccountManager getAccountManager() {
    if (accountManager == null) {

      OkHttpClient.Builder okhttpBuilder =
          new OkHttpClient.Builder().writeTimeout(30, TimeUnit.SECONDS)
              .readTimeout(30, TimeUnit.SECONDS)
              .connectTimeout(60, TimeUnit.SECONDS)
              .addInterceptor(getConnectivityInterceptor())
              .addInterceptor(getUserAgentInterceptor());

      final Retrofit retrofitV3 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(okhttpBuilder.build())
          .baseUrl("http://webservices.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      final Retrofit retrofitV7 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(okhttpBuilder.build())
          .baseUrl("http://ws75.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      accountManager = new AptoideAccountManager(
          new RetrofitAccountService(retrofitV3.create(RetrofitAccountService.ServiceV3.class),
              retrofitV7.create(RetrofitAccountService.ServiceV7.class), new SecurityAlgorithms(),
              new AccountResponseMapper(), getAuthenticationProvider()),
          new SharedPreferencesAccountPersistence(PublishSubject.create(),
              PreferenceManager.getDefaultSharedPreferences(this), Schedulers.io()),
          new CredentialsValidator(), getSocialLogoutManager(), getAppContext());
    }
    return accountManager;
  }

  public AuthenticationProvider getAuthenticationProvider() {
    if (authenticationProvider == null) {

      OkHttpClient.Builder okhttpBuilder =
          new OkHttpClient.Builder().writeTimeout(30, TimeUnit.SECONDS)
              .readTimeout(30, TimeUnit.SECONDS)
              .connectTimeout(60, TimeUnit.SECONDS)
              .addInterceptor(getConnectivityInterceptor())
              .addInterceptor(getUserAgentInterceptor());

      final Retrofit retrofitV3 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(okhttpBuilder.build())
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

  public CategoriesManager getCategoriesManager() {
    if (categoriesManager == null) {

      OkHttpClient.Builder okhttpBuilder =
          new OkHttpClient.Builder().addInterceptor(getTokenRevalidationInterceptorV7())
              .addInterceptor(getConnectivityInterceptor())
              .addInterceptor(getUserAgentInterceptor());

      final Retrofit retrofitV7 = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(okhttpBuilder.build())
          .baseUrl("https://ws75.aptoide.com/api/7/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      categoriesManager = new CategoriesManager(new RetrofitCategoriesService(
          retrofitV7.create(RetrofitCategoriesService.ServiceV7.class)));
    }
    return categoriesManager;
  }

  public StoreManager getAppsManager() {
    if (storeManager == null) {

      storeManager = new StoreManager(new PackageManagerInstalledAppsProvider(getPackageManager()),
          new AccountStoreNameProvider(getAccountManager()), getUploadManager(),
          getLanguageManager(), getAccountManager());
    }
    return storeManager;
  }

  public AppUploadStatusManager getAppUploadStatusManager() {
    if (appUploadStatusManager == null) {

      OkHttpClient.Builder okhttpBuilder =
          new OkHttpClient.Builder().addInterceptor(getTokenRevalidationInterceptorV7())
              .addInterceptor(getConnectivityInterceptor())
              .addInterceptor(getUserAgentInterceptor());

      final Retrofit retrofitV7Secondary = new Retrofit.Builder().addCallAdapterFactory(
          RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .client(okhttpBuilder.build())
          .baseUrl("http://ws75-secondary.aptoide.com/")
          .addConverterFactory(MoshiConverterFactory.create())
          .build();

      appUploadStatusManager =
          new AppUploadStatusManager(new AccountStoreNameProvider(getAccountManager()),
              new RetrofitStoreService(
                  retrofitV7Secondary.create(RetrofitStoreService.ServiceV7.class),
                  getAccessTokenProvider()), new RetrofitAppsUploadStatusService(
              retrofitV7Secondary.create(RetrofitAppsUploadStatusService.ServiceV7.class),
              getAccessTokenProvider()),
              new PackageManagerInstalledAppsProvider(getPackageManager()), getUploaderAnalytics());
    }
    return appUploadStatusManager;
  }

  private AptoideAccountProvider getAccessTokenProvider() {
    return new AptoideAccountProvider(getAccountManager(), getAuthenticationProvider());
  }

  public AptoideConnectivityProvider getConnectivityProvider() {
    return new AptoideConnectivityProvider(getApplicationContext());
  }

  public LanguageManager getLanguageManager() {
    if (languageManager == null) {
      languageManager = new AndroidLanguageManager();
    }
    return languageManager;
  }

  public OkioMd5Calculator getMd5Calculator() {
    if (md5Calculator == null) {
      md5Calculator = new OkioMd5Calculator(new HashMap<>(), Schedulers.computation());
    }
    return md5Calculator;
  }

  public UploaderPersistence getUploadPersistence() {
    if (uploadPersistence == null) {
      uploadPersistence = new MemoryUploaderPersistence(new HashMap<>(), Schedulers.trampoline());
    }
    return uploadPersistence;
  }

  public AppUploadStatusPersistence getAppUploadStatusPersistence() {
    if (appUploadStatusPersistence == null) {
      appUploadStatusPersistence =
          new MemoryAppUploadStatusPersistence(new HashMap<>(), Schedulers.trampoline());
    }
    return appUploadStatusPersistence;
  }

  public UploaderAnalytics getUploaderAnalytics() {
    if (uploaderAnalytics == null) {
      uploaderAnalytics = new UploaderAnalytics(AppEventsLogger.newLogger(this));
    }
    return uploaderAnalytics;
  }

  public void startFlurryAgent() {
    new FlurryAgent.Builder().withLogEnabled(true)
        .build(this, BuildConfig.FLURRY_KEY_UPLOADER);
  }

  public CallbackManager getCallbackManager() {
    return callbackManager;
  }

  public GoogleSignInOptions getGSO() {
    return new GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN).requestServerAuthCode(getString(R.string.google_id))
        .requestEmail()
        .build();
  }

  public SocialLogoutManager getSocialLogoutManager() {
    return new SocialLogoutManager(getAppContext(), getGSO());
  }

  public AutoLoginManager getAutoLoginManager() {
    return new AutoLoginManager(getAppContext());
  }

  public Context getAppContext() {
    return getApplicationContext();
  }

  public void setForcedLogout(boolean isForcedLogout) {
    forcedLogout = isForcedLogout;
  }

  public boolean isForcedLogout() {
    return forcedLogout;
  }
}
