package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.view.View;

public interface NotificationView extends View {

  void showDuplicateUploadNotification(String applicationName, String packageName);

  void showCompletedUploadNotification(String applicationName, String packageName);

  void showPendingUploadNotification(String applicationName, String packageName);

  void showNoMetaDataNotification(String applicationName, String packageName, String md5);

  void showPublisherOnlyNotification(String applicationName, String packageName);

  void showUploadInfectionNotificaton(String applicationName, String packageName);

  void showIntellectualRightsNotification(String applicationName, String packageName);

  void showAppBundleNotification(String applicationName, String packageName);

  void showAntiSpamRuleNotification(String applicationName, String packageName);

  void showInvalidSignatureNotification(String applicationName, String packageName);

  void updateUploadProgress(String name, String packageName, int progress);

  void showUnknownErrorRetryNotification(String name, String packageName);

  void showFailedUploadWithRetryNotification(String applicationName, String packageName);

  void showFailedUploadNotification(String applicationName, String packageName);

  void showUnknownErrorNotification(String applicationName, String packageName);

  void showGetRetriesExceededNotification(String applicationName, String packageName);

  void showCatappultCertifiedNotification(String appName, String packageName);
}

