package com.aptoide.uploader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.view.NotificationPresenter;
import com.aptoide.uploader.apps.view.NotificationView;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class NotificationApplicationView extends Service implements NotificationView {

  private final String NOTIFICATION_CHANNEL_ID = "Upload";
  private final int NOTIFICATION_ID_CHANGER = 50;
  private UploadManager uploadManager;
  private BehaviorSubject<LifecycleEvent> lifecycleSubject;
  private NotificationManager notificationManager;
  private NotificationPresenter systemNotificationShower;

  @Override public void onCreate() {
    super.onCreate();
    Log.i("LOL", "Service has been started onCreate");
    lifecycleSubject = BehaviorSubject.create();
    lifecycleSubject.onNext(LifecycleEvent.CREATE);
    uploadManager = ((UploaderApplication) getApplicationContext()).getUploadManager();
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    systemNotificationShower = new NotificationPresenter(this, getUploadManager());
    attachPresenter();
    setupChannels();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i("LOL", "Service has been started");
    return START_STICKY;
  }

  @Override public void onDestroy() {
    Log.i("LOL", "Service has been destroyed");
    super.onDestroy();
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public Observable<LifecycleEvent> getLifecycleEvent() {
    return lifecycleSubject;
  }

  @Override
  public void showDuplicateUploadNotification(String applicationName, String packageName) {
    Log.d("notificationz4",
        "showing duplicated notification " + packageName + " " + applicationName);
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_duplicate_upload)).setOngoing(false);
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override
  public void showCompletedUploadNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_success_upload)).setProgress(0, 0,
        false)
        .setOngoing(false);
    Log.d("notificationz4", "showing success notification " + packageName + " " + applicationName);
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showPendingUploadNotification(String applicationName, String packageName) {
    Log.d("notificationz4", "showing pending notification " + packageName + " " + applicationName);

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setContentTitle(applicationName)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true);
    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);
    startForeground(packageName.hashCode() + NOTIFICATION_ID_CHANGER, mBuilder.build());
  }

  @Override
  public void showNoMetaDataNotification(String applicationName, String packageName, String md5) {

    final Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.setAction("navigateToSubmitAppFragment");
    intent.putExtra("md5", md5);
    intent.putExtra("appName", applicationName);
    final PendingIntent contentIntent =
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    final Intent deleteIntent = new Intent(this, MainActivity.class);
    deleteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    deleteIntent.setAction("dismissNotification");
    deleteIntent.putExtra("md5", md5);
    deleteIntent.putExtra("appName", applicationName);
    final PendingIntent dismissIntent =
        PendingIntent.getActivity(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setOngoing(false)
            .setContentText(
                getString(R.string.application_notification_message_app_no_metadata_upload))
            .setContentTitle(applicationName)
            .setContentIntent(contentIntent)
            .setDeleteIntent(dismissIntent)
            .setAutoCancel(true);
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showPublisherOnlyNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_publisher_only));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showUploadInfectionNotificaton(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_infection));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override
  public void showIntellectualRightsNotification(String applicationName, String packageName) {
    Log.d("notificationz4",
        "showing intellectual rights notification " + packageName + " " + applicationName);

    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_intellectual_property));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showAppBundleNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_bundle));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showAntiSpamRuleNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_anti_spam));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override
  public void showInvalidSignatureNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_invalid_signature));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override
  public void updateUploadProgress(String applicationName, String packageName, int progress) {
    Log.d("notificationz4",
        "showing progress notification " + packageName + " " + applicationName + " " + progress);
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setContentTitle(applicationName)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false);
    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);
    startForeground(packageName.hashCode() + NOTIFICATION_ID_CHANGER, mBuilder.build());
  }

  @Override
  public void showUnknownErrorRetryNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_message_error_retry));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override
  public void showFailedUploadWithRetryNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_upload_failed_retry));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showFailedUploadNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_upload_failed));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showUnknownErrorNotification(String applicationName, String packageName) {
    Log.d("notificationz4",
        "showing unknown error notification " + packageName + " " + applicationName);

    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_message_error));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override
  public void showGetRetriesExceededNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_get_retries_exceeded));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  @Override public void showCatappultCertifiedNotification(String appName, String packageName) {
    Log.d("notificationz4", "showing catappult notification " + packageName + " " + appName);

    NotificationCompat.Builder mBuilder = buildNotification(appName,
        getString(R.string.application_notification_short_app_intellectual_property_certified));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
    notificationManager.cancel(packageName.hashCode() + NOTIFICATION_ID_CHANGER);
  }

  public NotificationCompat.Builder buildNotification(String applicationName, String subText) {

    final Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.setAction("navigateToMyStoreFragment");
    final PendingIntent contentIntent =
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
        R.drawable.notification_icon)
        .setContentTitle(applicationName)
        .setOngoing(false)
        .setContentText(subText)
        .setAutoCancel(true)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(subText))
        .setContentIntent(contentIntent);
  }

  public void setupChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationManager notificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      NotificationChannel channelUpload = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Upload",
          NotificationManager.IMPORTANCE_LOW);
      channelUpload.setDescription("Upload Information Notification");
      notificationManager.createNotificationChannel(channelUpload);
    }
  }

  private void attachPresenter() {
    systemNotificationShower.present();
  }

  public UploadManager getUploadManager() {
    return this.uploadManager;
  }
}
