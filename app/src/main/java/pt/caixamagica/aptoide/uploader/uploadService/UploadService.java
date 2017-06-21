/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.uploadService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.facebook.AppEventsLogger;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.ValidationException;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pt.caixamagica.aptoide.uploader.R;
import pt.caixamagica.aptoide.uploader.SelectablePackageInfo;
import pt.caixamagica.aptoide.uploader.UploaderUtils;
import pt.caixamagica.aptoide.uploader.activities.SubmitActivity;
import pt.caixamagica.aptoide.uploader.analytics.UploaderAnalytics;
import pt.caixamagica.aptoide.uploader.retrofit.RetrofitSpiceServiceUploadService;
import pt.caixamagica.aptoide.uploader.retrofit.request.StoreTokenInterface;
import pt.caixamagica.aptoide.uploader.retrofit.request.UploadAppToRepoRequest;
import pt.caixamagica.aptoide.uploader.webservices.json.Error;
import pt.caixamagica.aptoide.uploader.webservices.json.UploadAppToRepoJson;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

import static pt.caixamagica.aptoide.uploader.activities.LoginActivity.SALT;
import static pt.caixamagica.aptoide.uploader.activities.LoginActivity.SHARED_PREFERENCES_FILE;

/**
 * Created by neuro on 07-03-2015.
 */
public class UploadService extends Service {

  private static final String TAG = UploadService.class.getSimpleName();
  private final MyBinder myBinder = new MyBinder(this);
  public String inputTitle = null;
  public String languageCode = null;
  protected SpiceManager spiceManager = new SpiceManager(RetrofitSpiceServiceUploadService.class);
  NotificationManager mNotificationManager;
  private UploaderAnalytics uploaderAnalytics;
  private UserCredentialsJson userCredentialsJson;
  private Map<String, UploadAppToRepoRequest> sendingAppsUploadRequests = new HashMap<>();
  private Map<String, SelectablePackageInfo> sendingAppsSelectablePackageInfos = new HashMap<>();

  private NotificationCompat.Builder setPreparingUploadNotification(String packageName,
      String label) {
    return setNotification(packageName, getString(R.string.upload_prepare, label),
        newCancelIntent(packageName));
  }

  private NotificationCompat.Builder setNotification(String packageName, String text,
      PendingIntent pendingIntent) {

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplication());
    Bitmap b = loadIcon(packageName);
    if (b != null) mBuilder.setLargeIcon(b);
    b = null;
    mBuilder.setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getApplication().getString(R.string.app_name))
        .setContentIntent(buildRetryIntent(packageName))
        .setOngoing(false)
        .setContentText(text);

    if (pendingIntent != null) mBuilder.setContentIntent(pendingIntent);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());

    return mBuilder;
  }

  private void cancelUpload(String packageName) {
    UploadAppToRepoRequest uploadAppToRepoRequest = sendingAppsUploadRequests.get(packageName);

    if (uploadAppToRepoRequest != null) {
      uploadAppToRepoRequest.cancel();

      setRetryNotification(uploadAppToRepoRequest.getPackageName(),
          uploadAppToRepoRequest.getLabel());
    }
  }

  public void prepareUploadAndSend(final UserCredentialsJson userCredentialsJson,
      SelectablePackageInfo packageInfo) throws ValidationException {

    if (this.userCredentialsJson == null) this.userCredentialsJson = userCredentialsJson;

    // Parece me a mim k esta intent não serve para nada.... ou pode nao servir.
    Intent intent = new Intent(getApplication(), UploadService.class);
    intent.putExtra("userCredentialsJson", userCredentialsJson);
    intent.putExtra("packageInfo", packageInfo);
    intent.putExtra("appName", packageInfo.getLabel());

    NotificationCompat.Builder builder =
        setPreparingUploadNotification(packageInfo.packageName, packageInfo.getLabel());

    RequestProgressListener requestProgressListener =
        new RequestProgressListener(getApplication(), intent, builder);
    final SharedPreferences sharedpreferences =
        getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

    final AESObfuscator aesObfuscator = new AESObfuscator(SALT, getPackageName(),
        Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

    UploadAppToRepoRequest uploadAppToRepoRequest =
        new UploadAppToRepoRequest(requestProgressListener, new StoreTokenInterface() {
          @Override public void setToken(String token) {
            sharedpreferences.edit()
                .putString("token", aesObfuscator.obfuscate(token, "token"))
                .apply();
          }
        });
    uploadAppToRepoRequest.setPackageName(packageInfo.packageName);
    uploadAppToRepoRequest.setApkName(packageInfo.getName());
    uploadAppToRepoRequest.setToken(
        aesObfuscator.unobfuscate(sharedpreferences.getString("token", ""), "token"));
    uploadAppToRepoRequest.setApkPath(packageInfo.applicationInfo.sourceDir);
    uploadAppToRepoRequest.setRepo(userCredentialsJson.getRepo());

    uploadAppToRepoRequest.setLabel(packageInfo.getLabel());

    // Campos editáveis
    if ("".equals(packageInfo.getDescription())) {
      packageInfo.setDescription(packageInfo.getName());
    }
    uploadAppToRepoRequest.setDescription(packageInfo.getDescription());
    uploadAppToRepoRequest.setCategory(packageInfo.getCategory());
    uploadAppToRepoRequest.setRating(packageInfo.getAgeRating());

    if (inputTitle != null) {
      //These only apply when coming from a successfull getProposed request
      uploadAppToRepoRequest.setInputTitle(inputTitle);
      uploadAppToRepoRequest.setApkName(inputTitle);
    } else {
      uploadAppToRepoRequest.setInputTitle(packageInfo.getName());
    }
    uploadAppToRepoRequest.setLang(packageInfo.getLang());
    uploadApp(uploadAppToRepoRequest, packageInfo);
    inputTitle = null;
  }

  private void uploadApp(final UploadAppToRepoRequest uploadAppToRepoRequest,
      final SelectablePackageInfo selectablePackageInfo) {

    sendingAppsUploadRequests.put(uploadAppToRepoRequest.getPackageName(), uploadAppToRepoRequest);
    sendingAppsSelectablePackageInfos.put(uploadAppToRepoRequest.getPackageName(),
        selectablePackageInfo);

    spiceManager.execute(uploadAppToRepoRequest, new RequestListener<UploadAppToRepoJson>() {
      @Override public void onRequestFailure(SpiceException spiceException) {
        Log.e(TAG, "onRequestFailure: ", spiceException);

        if (spiceException instanceof NetworkException) {
          checkUploadSuccess(uploadAppToRepoRequest);
          //setNotification(uploadAppToRepoRequest.getPackageName(),
          //    getString(R.string.upload_done_network_error), null);
        } else {
          uploaderAnalytics.uploadComplete("fail", "Upload App to Repo");
          setRetryNotification(uploadAppToRepoRequest.getPackageName(),
              uploadAppToRepoRequest.getLabel());
        }
      }

      private boolean isDummyUploadError(List<Error> errors) {
        return hasErrorCodes(errors, getString(R.string.error_marg_100),
            getString(R.string.error_marg_101), getString(R.string.error_marg_102),
            getString(R.string.error_marg_103));
      }      // Investigar powerlock

      private boolean systemError(List<Error> errors) {
        return hasErrorCode(errors, "SYS-1");
      }

      private boolean processErrors(
          List<pt.caixamagica.aptoide.uploader.webservices.json.Error> errorList) {

        boolean missingBinaryFound = false;

        for (Error error : errorList) {
          if (!missingBinaryFound) {
            missingBinaryFound = setErrorFlag(error);
          } else {
            setErrorFlag(error);
          }
        }

        return missingBinaryFound;
      }

      private boolean hasErrorCodes(List<Error> errors, String... errCodes) {
        for (String errCode : errCodes) {
          if (hasErrorCode(errors, errCode)) {
            return true;
          }
        }

        return false;
      }

      private boolean hasErrorCode(List<Error> errors, String errCode) {
        for (Error e : errors) {
          if (e.getCode()
              .equals(errCode)) {
            return true;
          }
        }

        return false;
      }

      private boolean setErrorFlag(Error error) {
        String errorCode = error.getCode();

        switch (errorCode) {
          case "APK-5":
            uploadAppToRepoRequest.setFLAG_APK(true);
            break;
          case "OBB-1":
            uploadAppToRepoRequest.setFLAG_MAIN_OBB(true);
            break;
          case "OBB-2":
            uploadAppToRepoRequest.setFLAG_PATCH_OBB(true);
            break;
          default:
            return false;
        }

        return true;
      }

      @Override public void onRequestSuccess(UploadAppToRepoJson uploadAppToRepoJson) {
        Log.v(TAG, "onRequestSuccess: ");
        if (uploadAppToRepoJson.getErrors() != null) {

          //					if (uploadAppToRepoJson.getErrors().get(0).getCode().equals("AUTH-2")) {
          //						OAuth2AuthenticationRequest oAuth2AuthenticationRequest = new OAuth2AuthenticationRequest();
          //						oAuth2AuthenticationRequest.bean.setGrant_type("refresh_token");
          //						oAuth2AuthenticationRequest.bean.setRefresh_token(userCredentialsJson.getRefreshToken());
          //						spiceManager.execute(oAuth2AuthenticationRequest, new RequestListener<OAuth>() {
          //							@Override
          //							public void onRequestFailure(SpiceException spiceException) {
          //
          //							}
          //
          //							@Override
          //							public void onRequestSuccess(OAuth oAuth) {
          //								userCredentialsJson.setToken(oAuth.getAccess_token());
          //								Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_LONG).show();
          //							}
          //						});
          //					}

          // Don't show notification if the problem is lack of info
          if (isDummyUploadError(uploadAppToRepoJson.getErrors())) {
            setFillMissingInfoNotification(uploadAppToRepoRequest, selectablePackageInfo);
            return;
          }

          if (systemError(uploadAppToRepoJson.getErrors())) {
            uploaderAnalytics.uploadComplete("fail", "Upload App to Repo");
            setRetryNotification(uploadAppToRepoRequest.getPackageName(),
                uploadAppToRepoRequest.getLabel());
          } else {
            boolean retry = processErrors(uploadAppToRepoJson.getErrors());

            // Reenvia o pedido com os campos em falta
            if (retry) {
              uploadApp(uploadAppToRepoRequest, selectablePackageInfo);
            } else {
              uploaderAnalytics.uploadComplete("fail", "Upload App to Repo");
              List<Error> errors = uploadAppToRepoJson.getErrors();
              String packageName = uploadAppToRepoRequest.getPackageName();
              String label = uploadAppToRepoRequest.getLabel();
              setErrorsNotification(packageName, label, errors);
            }
          }
        } else {
          uploaderAnalytics.uploadComplete("success", "Upload App to Repo");
          setFinishedNotification(uploadAppToRepoRequest.getPackageName(),
              uploadAppToRepoRequest.getLabel());
          sendingAppsUploadRequests.remove(uploadAppToRepoRequest.getPackageName());
          sendingAppsSelectablePackageInfos.remove(uploadAppToRepoRequest.getPackageName());
        }
      }
    });
  }

  private void setFillMissingInfoNotification(UploadAppToRepoRequest uploadAppToRepoJson,
      SelectablePackageInfo selectablePackageInfo) {

    String packageName = uploadAppToRepoJson.getPackageName();
    String label = uploadAppToRepoJson.getLabel();

    NotificationCompat.Builder mBuilder = new NotificationCompat.
        Builder(getApplication());
    Bitmap b = loadIcon(packageName);
    if (b != null) mBuilder.setLargeIcon(b);
    b = null;
    mBuilder.setSmallIcon(R.drawable.notification_icon)
        .
            setContentTitle(getApplication().getString(R.string.app_name))
        .
            setContentIntent(buildFillMissingInfoIntent(selectablePackageInfo))
        .
            setOngoing(false)
        .
            setAutoCancel(true)
        .
            setStyle(
                new NotificationCompat.BigTextStyle().bigText(getString(R.string.upload_more_info)))
        .
            setSubText(getString(R.string.upload_more_info))
        .
            setContentText(label);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  private void setFinishedNotification(String packageName, String label) {
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplication());
    Bitmap b = loadIcon(packageName);
    if (b != null) mBuilder.setLargeIcon(b);
    b = null;
    mBuilder.setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getString(R.string.app_name))
        .setOngoing(false)
        .setSubText("App successfully uploaded")
        .setContentText(label);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  private void simpleNotification(String packageName, String label, String message) {
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(getApplication());
    Bitmap icon = loadIcon(packageName);
    if (icon != null) {
      notificationBuilder.setLargeIcon(icon);
    }
    icon = null;
    notificationBuilder.setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getString(R.string.app_name))
        .setOngoing(false)
        .setSubText(message)
        .setContentText(label);

    NotificationManager notificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(packageName.hashCode(), notificationBuilder.build());
  }

  @Override public void onCreate() {
    spiceManager.start(this);
    uploaderAnalytics = new UploaderAnalytics(AppEventsLogger.newLogger(getApplicationContext()));
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {

    if (mNotificationManager == null) {
      mNotificationManager =
          (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    if (intent != null) {
      if (getString(R.string.cancel).equals(intent.getAction())) {
        cancelUpload(intent.getExtras()
            .getString("packageName"));
      }
      if (getString(R.string.retry).equals(intent.getAction())) {
        UploadAppToRepoRequest req =
            sendingAppsUploadRequests.get(intent.getStringExtra("packageName"));

        if (req != null) {

          setPreparingUploadNotification(req.getPackageName(), req.getLabel());

          uploadApp(new UploadAppToRepoRequest(req, new StoreTokenInterface() {
            @Override public void setToken(String token) {
              final SharedPreferences sharedpreferences =
                  getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

              final AESObfuscator aesObfuscator = new AESObfuscator(SALT, getPackageName(),
                  Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
              sharedpreferences.edit()
                  .putString("token", aesObfuscator.obfuscate(token, "token"))
                  .apply();
            }
          }), null);
        }
      }
    }

    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onDestroy() {
    spiceManager.shouldStop();
  }

  @Override public IBinder onBind(Intent intent) {
    return myBinder;
  }

  @Override public boolean onUnbind(Intent intent) {
    return super.onUnbind(intent);
  }

  private void setRetryNotification(String packageName, String label) {
    uploaderAnalytics.uploadComplete("fail", "Upload App to Repo");
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplication());
    Bitmap b = loadIcon(packageName);
    if (b != null) mBuilder.setLargeIcon(b);
    b = null;
    mBuilder.setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getApplication().getString(R.string.app_name))
        .setContentIntent(buildRetryIntent(packageName))
        .setOngoing(false)
        .setSubText("Tap to retry, " + "slide" + " " + "" + "to dismiss")
        .setContentText(label);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  private void setErrorsNotification(String packageName, String label, List<Error> errors) {

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplication());
    Bitmap b = loadIcon(packageName);
    if (b != null) mBuilder.setLargeIcon(b);
    b = null;
    mBuilder.setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getApplication().getString(R.string.app_name))
        .setOngoing(false)
        .setContentText(label);

    if (Build.VERSION.SDK_INT >= 16) {
      mBuilder.setSubText("Error Uploading (Expand for details)");
    }

    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

    // Build Text
    StringBuilder stringBuilder = new StringBuilder();
    boolean newLine = false;
    for (Error error : errors) {
      if (newLine) {
        stringBuilder.append("\n");
      } else {
        newLine = true;
      }

      stringBuilder.append(error.getCode())
          .append(": ")
          .append(error.getMsg());
    }

    bigTextStyle.bigText(stringBuilder.toString());
    mBuilder.setStyle(bigTextStyle);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  private android.graphics.Bitmap loadIcon(String packageName) {
    Drawable drawable = null;
    try {
      drawable = getPackageManager().getPackageInfo(packageName, 0).applicationInfo.loadIcon(
          getPackageManager());
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
  }

  private PendingIntent buildFillMissingInfoIntent(SelectablePackageInfo selectablePackageInfo) {
    Intent intent = new Intent(getApplicationContext(), SubmitActivity.class);

    intent.setAction(SubmitActivity.FILL_MISSING_INFO);

    intent.putExtra("userCredentialsJson", userCredentialsJson);
    intent.putExtra("selectablePackageInfo", selectablePackageInfo);

    return PendingIntent.getActivity(getApplication(), uniqueId(), intent, 0);
  }

  private PendingIntent buildRetryIntent(String apkName) {
    Intent intent = new Intent(getApplicationContext(), UploadService.class);

    intent.setAction(getString(R.string.retry));

    intent.putExtra("packageName", apkName);

    return PendingIntent.getService(getApplication(), uniqueId(), intent, 0);
  }

  private int uniqueId() {
    return (int) System.currentTimeMillis() / 1000;
  }

  private PendingIntent newCancelIntent(String packageName) {

    // Gera um ID único para prevenir reutilização de PendingIntent.
    int reqCode = (int) System.currentTimeMillis() / 1000;

    Intent intent = new Intent(this, UploadService.class);

    intent.setAction(getString(R.string.cancel));
    intent.putExtra("packageName", packageName);

    return PendingIntent.getService(this, reqCode, intent, 0);
  }

  private void checkUploadSuccess(final UploadAppToRepoRequest originalRequest) {
    final SharedPreferences sharedpreferences =
        getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

    final AESObfuscator aesObfuscator = new AESObfuscator(SALT, getPackageName(),
        Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

    final UploadAppToRepoRequest uploadAppToRepoRequest =
        new UploadAppToRepoRequest(originalRequest, new StoreTokenInterface() {
          @Override public void setToken(String token) {
            sharedpreferences.edit()
                .putString("token", aesObfuscator.obfuscate(originalRequest.getToken(), "token"))
                .apply();
          }
        });
    uploadAppToRepoRequest.setApkMd5sum(
        UploaderUtils.md5Calc(new File(originalRequest.getApkPath())));
    spiceManager.execute(uploadAppToRepoRequest, new RequestListener<UploadAppToRepoJson>() {
      @Override public void onRequestFailure(SpiceException spiceException) {
        uploaderAnalytics.uploadComplete("fail", "Check if in Store");
        simpleNotification(uploadAppToRepoRequest.getPackageName(),
            uploadAppToRepoRequest.getLabel(), getString(R.string.upload_failed));
      }

      @Override public void onRequestSuccess(UploadAppToRepoJson uploadAppToRepoJson) {
        if (uploadAppToRepoJson.getErrors() != null) {
          String errorCode = uploadAppToRepoJson.getErrors()
              .get(0)
              .getCode();
          if (errorCode.equals("APK-5")) {
            uploaderAnalytics.uploadComplete("fail", "Check if in Store");
            setErrorsNotification(uploadAppToRepoRequest.getPackageName(),
                uploadAppToRepoRequest.getLabel(), uploadAppToRepoJson.getErrors());
          } else if (errorCode.equals("APK-103")) {
            uploaderAnalytics.uploadComplete("success", "Check if in Store");
            setFinishedNotification(uploadAppToRepoRequest.getPackageName(),
                uploadAppToRepoRequest.getLabel());
            sendingAppsUploadRequests.remove(uploadAppToRepoRequest.getPackageName());
            sendingAppsSelectablePackageInfos.remove(uploadAppToRepoRequest.getPackageName());
          } else {
            uploaderAnalytics.uploadComplete("fail", "Check if in Store");
            simpleNotification(uploadAppToRepoRequest.getPackageName(),
                uploadAppToRepoRequest.getLabel(), getString(R.string.upload_failed));
          }
        } else {
          uploaderAnalytics.uploadComplete("success", "Check if in Store");
          setFinishedNotification(uploadAppToRepoRequest.getPackageName(),
              uploadAppToRepoRequest.getLabel());
          sendingAppsUploadRequests.remove(uploadAppToRepoRequest.getPackageName());
          sendingAppsSelectablePackageInfos.remove(uploadAppToRepoRequest.getPackageName());
        }
      }
    });
  }
}
