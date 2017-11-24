package com.aptoide.uploader.apps;

import io.reactivex.Single;

/**
 * Created by jdandrade on 24/11/2017.
 */

public interface StoreNameProvider {

  Single<String> getStoreName();
}
