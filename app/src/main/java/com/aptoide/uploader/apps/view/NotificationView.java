package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.view.View;

public interface NotificationView extends View {

  void showDuplicateUploadNotification(String applicationName, String packageName);

  void showCompletedUploadNotification(String applicationName, String packageName);

  void showPendingUploadNotification(String applicationName, String packageName);

  void showNoMetaDataNotification(String applicationName, String packageName, String md5);

  void showFailedUploadNotification(String applicationName, String packageName);

  void showPublisherOnlyNotification(String applicationName, String packageName);

  void showUploadInfectionNotificaton(String applicationName, String packageName);

  void showIntellectualRightsNotification(String applicationName, String packageName);

  void showInvalidSignatureNotification(String applicationName, String packageName);

  void updateUploadProgress(String name, String packageName, int progress);
}

