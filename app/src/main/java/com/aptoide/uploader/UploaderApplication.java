package com.aptoide.uploader;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;
import com.aptoide.authentication.AptoideAuthentication;
import com.aptoide.authentication.network.RemoteAuthenticationService;
import com.aptoide.authenticationrx.AptoideAuthenticationRx;
import com.aptoide.uploader.account.AgentPersistence;
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
import com.aptoide.uploader.apps.AutoUploadSelectsManager;
import com.aptoide.uploader.apps.CategoriesManager;
import com.aptoide.uploader.apps.InstallManager;
import com.aptoide.uploader.apps.InstalledAppsManager;
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
import com.aptoide.uploader.apps.network.RetrofitUploadService;
import com.aptoide.uploader.apps.network.TokenRevalidatorV7Alternate;
import com.aptoide.uploader.apps.network.UserAgentInterceptor;
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.apps.persistence.AppUploadsDatabase;
import com.aptoide.uploader.apps.persistence.DraftPersistence;
import com.aptoide.uploader.apps.persistence.MemoryDraftPersistence;
import com.aptoide.uploader.apps.persistence.RoomAutoUploadSelectsPersistence;
import com.aptoide.uploader.apps.persistence.RoomInstalledPersistence;
import com.aptoide.uploader.apps.persistence.RoomUploadStatusDataSource;
import com.aptoide.uploader.security.AptoideAccessTokenProvider;
import com.aptoide.uploader.security.AuthenticationPersistance;
import com.aptoide.uploader.security.AuthenticationProvider;
import com.aptoide.uploader.security.SharedPreferencesAuthenticationPersistence;
import com.aptoide.uploader.upload.AptoideAccountProvider;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import io.rakam.api.Rakam;
import io.rakam.api.RakamClient;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class UploaderApplication extends Application {
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();
  private AptoideAccountManager accountManager;
  private StoreManager storeManager;
  private UploadManager uploadManager;
  private InstallManager installManager;
  private AutoUploadSelectsManager autoUploadSelectsManager;
  private InstalledAppsManager installedAppsManager;
  private LanguageManager languageManager;
  private AuthenticationProvider authenticationProvider;
  private AptoideAuthenticationRx aptoideAuthenticationRx;
  private CategoriesManager categoriesManager;
  private OkioMd5Calculator md5Calculator;
  private AppUploadStatusPersistence appUploadStatusPersistence;
  private RoomInstalledPersistence roomInstalledPersistence;
  private RoomAutoUploadSelectsPersistence roomAutoUploadSelectsPersistence;
  private DraftPersistence draftPersistence;
  private AppUploadStatusManager appUploadStatusManager;
  private UploaderAnalytics uploaderAnalytics;
  private CallbackManager callbackManager;
  private PackageManagerInstalledAppsProvider packageManagerInstalledAppsProvider;
  private LoginManager loginManager;
  private AgentPersistence agentPersistence;

  @Override public void onCreate() {
    super.onCreate();
    startFlurryAgent();
    initializeRakam();
    getUploadManager().start();
    checkFirstRun();
  }

  public void checkFirstRun() {
    boolean isFirstRun = this.getSharedPreferences("PREFERENCE", 0)
        .getBoolean("isFirstRun", true);
    if (isFirstRun) {
      refreshInstalledApps();
      refreshAutoUploadSelection();
      this.getSharedPreferences("PREFERENCE", 0)
          .edit()
          .putBoolean("isFirstRun", false)
          .apply();
    }
  }

  private void refreshInstalledApps() {
    compositeDisposable.add(getInstallManager().insertAllInstalled()
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void refreshAutoUploadSelection() {
    compositeDisposable.add(getAutoUploadSelectsManager().insertAllInstalled()
        .subscribe(() -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  public UploadManager getUploadManager() {
    if (uploadManager == null) {

      final Retrofit retrofitV7 = retrofitBuilder("http://ws75-primary.aptoide.com/api/",
          buildOkHttpClient().addInterceptor(getTokenRevalidatorV7Alternate()));

      UploadProgressManager uploadProgressManager = new UploadProgressManager();

      uploadManager = new UploadManager(
          new RetrofitUploadService(retrofitV7.create(RetrofitUploadService.ServiceV7.class),
              getAccessTokenProvider(), uploadProgressManager, getUploaderAnalytics(),
              getMd5Calculator()), getMd5Calculator(),
          new ServiceBackgroundService(this, NotificationService.class), getAccessTokenProvider(),
          getAppUploadStatusManager(), getAppUploadStatusPersistence(), uploadProgressManager,
          getDraftPersistence());
    }
    return uploadManager;
  }

  public RoomInstalledPersistence getInstalledPersistence() {
    if (roomInstalledPersistence == null) {
      roomInstalledPersistence = new RoomInstalledPersistence(AppUploadsDatabase.getInstance(this)
          .installedDao());
    }
    return roomInstalledPersistence;
  }

  public RoomAutoUploadSelectsPersistence getAutoUploadSelectsPersistence() {
    if (roomAutoUploadSelectsPersistence == null) {
      roomAutoUploadSelectsPersistence = new RoomAutoUploadSelectsPersistence(
          AppUploadsDatabase.getInstance(this)
              .autoUploadSelectsDao()) {
      };
    }
    return roomAutoUploadSelectsPersistence;
  }

  public InstallManager getInstallManager() {
    if (installManager == null) {
      installManager =
          new InstallManager(getInstalledPersistence(), getAutoUploadSelectsPersistence(),
              getPackageManagerInstalledAppsProvider(), getInstalledAppsManager(),
              getAppsManager());
    }
    return installManager;
  }

  public AutoUploadSelectsManager getAutoUploadSelectsManager() {
    if (autoUploadSelectsManager == null) {
      autoUploadSelectsManager =
          new AutoUploadSelectsManager(getAutoUploadSelectsPersistence(), getPackageManager());
    }
    return autoUploadSelectsManager;
  }

  public InstalledAppsManager getInstalledAppsManager() {
    if (installedAppsManager == null) {

      installedAppsManager =
          new InstalledAppsManager(getInstalledPersistence(), getAppUploadStatusPersistence(),
              getAutoUploadSelectsPersistence(), Schedulers.io());
    }
    return installedAppsManager;
  }

  public OkHttpClient.Builder buildOkHttpClient() {
    return new OkHttpClient.Builder().writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(getConnectivityInterceptor())
        .addInterceptor(getUserAgentInterceptor());
  }

  public Retrofit retrofitBuilder(String baseUrl, OkHttpClient.Builder okHttpClient) {
    return new Retrofit.Builder().addCallAdapterFactory(
        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(okHttpClient.build())
        .build();
  }

  public IdsRepository getIdsRepository() {
    return new IdsRepository(PreferenceManager.getDefaultSharedPreferences(this));
  }

  public TokenRevalidatorV7Alternate getTokenRevalidatorV7Alternate() {
    return new TokenRevalidatorV7Alternate(getAuthenticationProvider());
  }

  public UserAgentInterceptor getUserAgentInterceptor() {
    return new UserAgentInterceptor(getIdsRepository());
  }

  public ConnectivityInterceptor getConnectivityInterceptor() {
    return new ConnectivityInterceptor(getApplicationContext());
  }

  public AptoideAccountManager getAccountManager() {
    if (accountManager == null) {

      final Retrofit retrofitV3 =
          retrofitBuilder("https://webservices.aptoide.com/", buildOkHttpClient());

      final Retrofit retrofitV7 = retrofitBuilder("https://ws75.aptoide.com/api/7/",
          buildOkHttpClient().addInterceptor(getTokenRevalidatorV7Alternate()));

      accountManager = new AptoideAccountManager(
          new RetrofitAccountService(retrofitV3.create(RetrofitAccountService.ServiceV3.class),
              retrofitV7.create(RetrofitAccountService.ServiceV7.class),
              new AccountResponseMapper(), getAuthenticationProvider(),
              getAptoideAuthenticationRx()),
          new SharedPreferencesAccountPersistence(PublishSubject.create(),
              PreferenceManager.getDefaultSharedPreferences(this), Schedulers.io()),
          new CredentialsValidator(), getSocialLogoutManager());
    }
    return accountManager;
  }

  public AptoideAuthenticationRx getAptoideAuthenticationRx() {
    if (aptoideAuthenticationRx == null) {
      aptoideAuthenticationRx = new AptoideAuthenticationRx(new AptoideAuthentication(
          new RemoteAuthenticationService("https://webservices.aptoide.com/api/7/",
              buildOkHttpClient().build())));
    }
    return aptoideAuthenticationRx;
  }

  public AuthenticationProvider getAuthenticationProvider() {
    if (authenticationProvider == null) {

      final Retrofit retrofitV3 =
          retrofitBuilder("https://webservices.aptoide.com/", buildOkHttpClient());

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

      final Retrofit retrofitV7 = retrofitBuilder("https://ws75.aptoide.com/api/7/",
          buildOkHttpClient().addInterceptor(getTokenRevalidatorV7Alternate()));

      categoriesManager = new CategoriesManager(new RetrofitCategoriesService(
          retrofitV7.create(RetrofitCategoriesService.ServiceV7.class)));
    }
    return categoriesManager;
  }

  public StoreManager getAppsManager() {
    if (storeManager == null) {

      storeManager = new StoreManager(getPackageManagerInstalledAppsProvider(),
          new AccountStoreNameProvider(getAccountManager()), getUploadManager(),
          getLanguageManager(), getAccountManager(), Schedulers.io(),
          new ServiceBackgroundService(getApplicationContext(), NotificationService.class));
    }
    return storeManager;
  }

  public AppUploadStatusManager getAppUploadStatusManager() {
    if (appUploadStatusManager == null) {

      final Retrofit retrofitV7Secondary =
          retrofitBuilder("https://ws75-secondary.aptoide.com/api/7/",
              buildOkHttpClient().addInterceptor(getTokenRevalidatorV7Alternate()));

      appUploadStatusManager =
          new AppUploadStatusManager(new AccountStoreNameProvider(getAccountManager()),
              new RetrofitAppsUploadStatusService(
                  retrofitV7Secondary.create(RetrofitAppsUploadStatusService.ServiceV7.class),
                  getAccessTokenProvider()), getPackageManagerInstalledAppsProvider(),
              getAppUploadStatusPersistence());
    }
    return appUploadStatusManager;
  }

  private PackageManagerInstalledAppsProvider getPackageManagerInstalledAppsProvider() {
    if (packageManagerInstalledAppsProvider == null) {
      packageManagerInstalledAppsProvider =
          new PackageManagerInstalledAppsProvider(getPackageManager(), Schedulers.io());
      return packageManagerInstalledAppsProvider;
    }
    return packageManagerInstalledAppsProvider;
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
      md5Calculator =
          new OkioMd5Calculator(new HashMap<>(), new HashMap<>(), Schedulers.computation());
    }
    return md5Calculator;
  }

  public DraftPersistence getDraftPersistence() {
    if (draftPersistence == null) {
      draftPersistence = new MemoryDraftPersistence(new HashMap<>(), Schedulers.io());
    }
    return draftPersistence;
  }

  public AppUploadStatusPersistence getAppUploadStatusPersistence() {
    if (appUploadStatusPersistence == null) {
      appUploadStatusPersistence = new RoomUploadStatusDataSource(
          AppUploadsDatabase.getInstance(getApplicationContext())
              .appUploadsStatusDao());
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
    if (callbackManager == null) {
      callbackManager = new CallbackManagerImpl();
    }
    return callbackManager;
  }

  public GoogleSignInOptions getGSO() {
    return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
        .requestScopes(new Scope("https://www.googleapis.com/auth/contacts.readonly"))
        .requestScopes(new Scope(Scopes.PROFILE))
        .requestServerAuthCode(getString(R.string.google_id))
        .build();
  }

  public SocialLogoutManager getSocialLogoutManager() {
    return new SocialLogoutManager(getApplicationContext(), getGSO());
  }

  public AutoLoginManager getAutoLoginManager() {
    return AutoLoginManager.getInstance(getApplicationContext());
  }

  private void initializeRakam() {
    RakamClient instance = Rakam.getInstance();

    String rakamBaseHost = BuildConfig.SCHEMA + "://" + BuildConfig.APTOIDE_WEB_SERVICES_RAKAM_HOST;

    try {
      instance.initialize(this, new URL(rakamBaseHost), BuildConfig.RAKAM_API_KEY);
    } catch (MalformedURLException e) {
      Log.e(getClass().getSimpleName(), "error: ", e);
    }
    instance.setDeviceId(getIdsRepository().getAndroidId());
    instance.enableForegroundTracking(this);
    instance.trackSessionEvents(true);
    instance.setLogLevel(Log.VERBOSE);
    instance.setEventUploadPeriodMillis(1);
    instance.setUserId(getIdsRepository().getUniqueIdentifier());

    JSONObject superProperties = Rakam.getInstance()
        .getSuperProperties();

    if (superProperties == null) {
      superProperties = new JSONObject();
    }

    try {
      superProperties.put("aptoide_package", getPackageName());
      superProperties.put("version_code", BuildConfig.VERSION_CODE);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    Rakam.getInstance()
        .setSuperProperties(superProperties);
  }

  public LoginManager getFacebookLoginManager() {
    if (loginManager == null) {
      loginManager = LoginManager.getInstance();
    }
    return loginManager;
  }

  public AgentPersistence getAgentPersistence() {
    if (agentPersistence == null) {
      this.agentPersistence =
          new AgentPersistence(PreferenceManager.getDefaultSharedPreferences(this));
    }
    return agentPersistence;
  }
}
