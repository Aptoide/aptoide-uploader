/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.uploadService;

import android.os.Binder;

/**
 * Created by neuro on 07-03-2015.
 */
public class MyBinder extends Binder {

  UploadService uploadService;

  public MyBinder(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  public UploadService getService() {
    return uploadService;
  }
}
