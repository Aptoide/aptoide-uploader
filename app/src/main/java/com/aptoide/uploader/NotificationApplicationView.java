package com.aptoide.uploader;

import android.app.Application;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.view.NotificationPresenter;
import com.aptoide.uploader.apps.view.NotificationView;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class NotificationApplicationView extends Application implements NotificationView {

  private BehaviorSubject<LifecycleEvent> lifecycleSubject;
  private final String NOTIFICATION_CHANNEL_ID = "Upload";
  private NotificationManager notificationManager;
  private NotificationPresenter systemNotificationShower;

  @Override public void onCreate() {
    super.onCreate();
    lifecycleSubject = BehaviorSubject.create();
    lifecycleSubject.onNext(LifecycleEvent.CREATE);
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    final Retrofit retrofitV7 = new Retrofit.Builder().addCallAdapterFactory(
        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .client(new OkHttpClient())
        .baseUrl("http://ws75.aptoide.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build();

    systemNotificationShower =
        new NotificationPresenter(this, ((UploaderApplication) this).getUploadManager());
    attachPresenter();
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

  private void attachPresenter() {
    systemNotificationShower.present();
  }
}
