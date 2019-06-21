package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.view.View;

public interface NotificationView extends View {

  void showDuplicateUploadNotification(String applicationName, String packageName);

  void showCompletedUploadNotification();

  void showProgressUploadNotification();

  void showPendingUploadNotification();

  void showNoMetaDataNotification(String applicationName, String packageName, String md5);
}
