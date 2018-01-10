package com.aptoide.uploader;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setContentTitle(getString(R.string.app_name))
            .setOngoing(false)
            .setSubText(getString(R.string.application_notification_short_app_duplicate_upload))
            .setContentText(applicationName);

    notificationManager.notify(packageName.hashCode(), mBuilder.build());
  }

  @Override public void showCompletedUploadNotification() {

  }

  @Override public void showProgressUploadNotification() {

  }

  @Override public void showPendingUploadNotification() {

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
