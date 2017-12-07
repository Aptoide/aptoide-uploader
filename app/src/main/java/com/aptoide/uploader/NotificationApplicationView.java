package com.aptoide.uploader;

import android.app.Application;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.view.NotificationView;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class NotificationApplicationView extends Application implements NotificationView {

  private BehaviorSubject<LifecycleEvent> lifecycleSubject;
  private final String NOTIFICATION_CHANNEL_ID = "Upload";
  private NotificationManager notificationManager;

  @Override public void onCreate() {
    super.onCreate();
    lifecycleSubject = BehaviorSubject.create();
    lifecycleSubject.onNext(LifecycleEvent.CREATE);
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
  }

  @Override public Observable<LifecycleEvent> getLifecycleEvent() {
    return lifecycleSubject;
  }

  @Override public void showCompletedUploadNotification(Upload upload) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(
            R.drawable.notification_icon)
            .setContentTitle(getString(R.string.app_name))
            .setOngoing(false)
            .setSubText(getString(R.string.application_notification_short_app_success_upload))
            .setContentText(upload.getInstalledApp()
                .getName());

    notificationManager.notify(upload.getInstalledApp()
        .getPackageName()
        .hashCode(), mBuilder.build());
  }
}
