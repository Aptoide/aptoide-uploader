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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.ValidationException;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pt.caixamagica.aptoide.uploader.R;
import pt.caixamagica.aptoide.uploader.SelectablePackageInfo;
import pt.caixamagica.aptoide.uploader.activities.SubmitActivity;
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

  public static final String UPLOADER_CANCEL = "cancel";
  public static final String UPLOADER_RETRY = "retry";

  private final MyBinder myBinder = new MyBinder(this);

  protected SpiceManager spiceManager = new SpiceManager(RetrofitSpiceServiceUploadService.class);

  NotificationManager mNotificationManager;
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

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(getApplication()).setLargeIcon(loadIcon(packageName))
            .setSmallIcon(R.drawable.notification_icon)
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

    uploadApp(uploadAppToRepoRequest, packageInfo);
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
          setNotification(uploadAppToRepoRequest.getPackageName(),
              getString(R.string.upload_done_network_error), null);
        } else {
          setRetryNotification(uploadAppToRepoRequest.getPackageName(),
              uploadAppToRepoRequest.getLabel());
        }
      }

      // Investigar powerlock
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
            setRetryNotification(uploadAppToRepoRequest.getPackageName(),
                uploadAppToRepoRequest.getLabel());
          } else {
            boolean retry = processErrors(uploadAppToRepoJson.getErrors());

            // Reenvia o pedido com os campos em falta
            if (retry) {
              uploadApp(uploadAppToRepoRequest, selectablePackageInfo);
            } else {
              List<Error> errors = uploadAppToRepoJson.getErrors();
              String packageName = uploadAppToRepoRequest.getPackageName();
              String label = uploadAppToRepoRequest.getLabel();
              setErrorsNotification(packageName, label, errors);
            }
          }
        } else {
          setFinishedNotification(uploadAppToRepoRequest.getPackageName(),
              uploadAppToRepoRequest.getLabel());
          sendingAppsUploadRequests.remove(uploadAppToRepoRequest.getPackageName());
          sendingAppsSelectablePackageInfos.remove(uploadAppToRepoRequest.getPackageName());
        }
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
          if (e.getCode().equals(errCode)) return true;
        }

        return false;
      }

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

      private boolean isDummyUploadError(List<Error> errors) {
        return hasErrorCodes(errors, "MARG-100", "MARG-101", "MARG-102", "MARG-103");
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
    });
  }

  private void setFillMissingInfoNotification(UploadAppToRepoRequest uploadAppToRepoJson,
      SelectablePackageInfo selectablePackageInfo) {

    String packageName = uploadAppToRepoJson.getPackageName();
    String label = uploadAppToRepoJson.getLabel();

    NotificationCompat.Builder mBuilder = new NotificationCompat.
        Builder(getApplication()).
        setLargeIcon(loadIcon(packageName)).
        setSmallIcon(R.drawable.notification_icon).
        setContentTitle(getApplication().getString(R.string.app_name)).
        setContentIntent(buildFillMissingInfoIntent(selectablePackageInfo)).
        setOngoing(false).
        setAutoCancel(true).
        setStyle(new NotificationCompat.BigTextStyle().bigText(
            "We need more info in order to upload your app, tap here in order to provide them.")).
        setSubText(
            "We need more info in order to upload your app, tap here in order to provide them.").
        setContentText(label);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  private void setFinishedNotification(String packageName, String label) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(getApplication()).setLargeIcon(loadIcon(packageName))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(getApplication().getString(R.string.app_name))
            .setOngoing(false)
            .setSubText("App successfully uploaded")
            .setContentText(label);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void onCreate() {
    spiceManager.start(this);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {

    if (mNotificationManager == null) {
      mNotificationManager =
          (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    if (intent != null) {
      if (UPLOADER_CANCEL.equals(intent.getAction())) {
        cancelUpload(intent.getExtras().getString("packageName"));
      }
      if (UPLOADER_RETRY.equals(intent.getAction())) {
        UploadAppToRepoRequest req =
            sendingAppsUploadRequests.get(intent.getStringExtra("packageName"));
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

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(getApplication()).setLargeIcon(loadIcon(packageName))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(getApplication().getString(R.string.app_name))
            .setContentIntent(buildRetryIntent(packageName))
            .setOngoing(false)
            .setSubText("Tap to retry, " +
                "slide" +
                " " +
                "" + "to dismiss")
            .setContentText(label);

    NotificationManager mNotificationManager =
        (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  private void setErrorsNotification(String packageName, String label, List<Error> errors) {

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(getApplication()).setLargeIcon(loadIcon(packageName))
            .setSmallIcon(R.drawable.notification_icon)
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

      stringBuilder.append(error.getCode()).append(": ").append(error.getMsg());
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

    return ((BitmapDrawable) drawable).getBitmap();
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

    intent.setAction(UploadService.UPLOADER_RETRY);

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

    intent.setAction(UploadService.UPLOADER_CANCEL);
    intent.putExtra("packageName", packageName);

    return PendingIntent.getService(this, reqCode, intent, 0);
  }
}
