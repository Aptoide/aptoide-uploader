/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import java.util.Set;

/**
 * Created by neuro on 06-03-2015.
 */
public class RetrofitSpiceServiceUploadService extends RetrofitSpiceServiceUploader {

  @Override public int getThreadCount() {
    return 1;
  }

  @Override public void addRequest(CachedSpiceRequest<?> request,
      Set<RequestListener<?>> listRequestListener) {
    request.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
    super.addRequest(request, listRequestListener);
  }
}
