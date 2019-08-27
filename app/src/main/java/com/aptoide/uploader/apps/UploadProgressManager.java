package com.aptoide.uploader.apps;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class UploadProgressManager implements UploadProgressListener {

  private PublishSubject<UploadProgress> uploadProgressListener;

  public UploadProgressManager() {
    this.uploadProgressListener = PublishSubject.create();
  }

  @Override public void updateProgress(int progress, String packageName) {
    uploadProgressListener.onNext(new UploadProgress(progress, packageName));
  }

  Observable<UploadProgress> getProgress(String packageName) {
    return uploadProgressListener.filter(uploadProgress -> uploadProgress.getPackageName()
        .equals(packageName));
  }
}
