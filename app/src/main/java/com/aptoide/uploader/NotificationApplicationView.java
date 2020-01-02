package com.aptoide.uploader;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.apps.view.NotificationPresenter;
import com.aptoide.uploader.apps.view.NotificationView;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public abstract class NotificationApplicationView extends Application implements NotificationView {

  private final String NOTIFICATION_CHANNEL_ID = "Upload";
  private BehaviorSubject<LifecycleEvent> lifecycleSubject;
  private NotificationManager notificationManager;
  private NotificationPresenter systemNotificationShower;

  @Override public void onCreate() {
    super.onCreate();
    lifecycleSubject = BehaviorSubject.create();
    lifecycleSubject.onNext(LifecycleEvent.CREATE);
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    systemNotificationShower = new NotificationPresenter(this, getUploadManager());
    attachPresenter();
    setupChannels();
  }

  @Override public Observable<LifecycleEvent> getLifecycleEvent() {
    return lifecycleSubject;
  }

  @Override
  public void showDuplicateUploadNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_duplicate_upload));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override
  public void showGetRetriesExceededNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_get_retries_exceeded));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }


  @Override
  public void showCompletedUploadNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_success_upload)).setProgress(0, 0,
        false);
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void showPendingUploadNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setContentTitle(applicationName)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true);

    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void showPublisherOnlyNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_publisher_only));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void showUploadInfectionNotificaton(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_infection));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override
  public void showIntellectualRightsNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_intellectual_property));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void showAppBundleNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_bundle));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override
  public void showInvalidSignatureNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_invalid_signature));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void showUnknownErrorNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_message_error));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override
  public void showFailedUploadWithRetryNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_upload_failed_retry));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override
  public void showUnknownErrorRetryNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_message_error_retry));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void showFailedUploadNotification(String applicationName, String packageName) {
    NotificationCompat.Builder mBuilder = buildNotification(applicationName,
        getString(R.string.application_notification_short_app_upload_failed));
    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override
  public void updateUploadProgress(String applicationName, String packageName, int progress) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setContentTitle(applicationName)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false);

    notificationManager.notify(packageName.hashCode(), mBuilder.build());
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

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setOngoing(false)
            .setContentText(
                getString(R.string.application_notification_message_app_no_metadata_upload))
            .setContentTitle(applicationName)
            .setContentIntent(contentIntent)
            .setAutoCancel(true);

    notificationManager.notify(packageName.hashCode(), mBuilder.build());
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
        .setContentIntent(contentIntent);
  }

  public void setupChannels() {
    if (Build.VERSION.SDK_INT >= 26) {
      NotificationManager notificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Channel name",
          NotificationManager.IMPORTANCE_DEFAULT);
      channel.setDescription("Channel description");
      notificationManager.createNotificationChannel(channel);
    }
  }

  private void attachPresenter() {
    systemNotificationShower.present();
  }

  public abstract UploadManager getUploadManager();
}
