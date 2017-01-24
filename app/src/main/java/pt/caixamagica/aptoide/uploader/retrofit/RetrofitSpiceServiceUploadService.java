/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit;

/**
 * Created by neuro on 06-03-2015.
 */
public class RetrofitSpiceServiceUploadService extends RetrofitSpiceServiceUploader {

  @Override public int getThreadCount() {
    return 1;
  }
}
