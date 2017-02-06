/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.uploadService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.octo.android.robospice.request.listener.RequestProgress;
import pt.caixamagica.aptoide.uploader.SelectablePackageInfo;

/**
 * Created by neuro on 04-03-2015.
 */
public class RequestProgressListener
    implements com.octo.android.robospice.request.listener.RequestProgressListener {

  private int mId;

  private NotificationCompat.Builder mBuilder;

  private NotificationManager mNotificationManager;

  private Context context;

  private SelectablePackageInfo packageInfo;

  public RequestProgressListener(Context context, Intent intent,
      NotificationCompat.Builder mBuilder) {
    this.context = context;
    this.mBuilder = mBuilder;
    mNotificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    processIntent(intent);

    // Depende do package name deb!!
    mId = packageInfo.packageName.hashCode();
    mBuilder.setContentText("Uploading " + packageInfo.getLabel() + " (Tap to cancel upload)");
  }

  private void processIntent(Intent intent) {
    packageInfo = (SelectablePackageInfo) intent.getExtras().get("packageInfo");
  }

  private PendingIntent newCancelationIntentV3() {

    // Gera um ID único para prevenir reutilização de PendingIntent.
    int reqCode = (int) System.currentTimeMillis() / 1000;

    Intent intent = new Intent(context, UploadService.class);

    intent.setAction(UploadService.UPLOADER_CANCEL);

    intent.putExtra("packageName", packageInfo.packageName);

    return PendingIntent.getService(context, reqCode, intent, 0);
  }

  @Override public void onRequestProgressUpdate(RequestProgress progress) {

    mBuilder.setProgress(100, (int) progress.getProgress(), false);

    mNotificationManager.notify(mId, mBuilder.build());

    if (progress.getProgress() >= 100) {
      mNotificationManager.notify(mId, mBuilder.build());
    }
  }
}