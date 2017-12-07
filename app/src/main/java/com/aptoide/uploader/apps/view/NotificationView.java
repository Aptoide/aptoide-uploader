package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.view.View;

public interface NotificationView extends View {

  void showCompletedUploadNotification(Upload upload);
}
