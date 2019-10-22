package com.aptoide.uploader.apps;

public interface UploadProgressListener {

  void updateProgress(int progress, String packageName);
}
