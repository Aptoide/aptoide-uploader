package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.ApksResponse;
import com.aptoide.uploader.apps.network.RetrofitStoreService;
import com.aptoide.uploader.apps.network.RetryWithDelay;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;

public class CheckAppsManager {

  private final StoreNameProvider storeNameProvider;
  private final RetrofitStoreService retrofitStoreService;

  public CheckAppsManager(StoreNameProvider storeNameProvider,
      RetrofitStoreService retrofitStoreService) {
    this.storeNameProvider = storeNameProvider;
    this.retrofitStoreService = retrofitStoreService;
  }

  public Observable<ApksResponse> getApks(List<String> md5List) {
    return storeNameProvider.getStoreName()
        .flatMapObservable(storeName -> retrofitStoreService.getApks(md5List, storeName));
  }

  public Observable<ApksResponse> checkUploadStatus(String md5) {
    return storeNameProvider.getStoreName()
        .flatMapObservable(storeName -> {
          List<String> md5List = new ArrayList<>();
          md5List.add(md5);
          return retrofitStoreService.getApks(md5List, storeName);
        })
        .flatMap(apksResponse -> {
          if (apksResponse.getList()
              .isEmpty()) {
            throw new RuntimeException();
          }
          return Observable.just(apksResponse);
        })
        .retryWhen(new RetryWithDelay(3, 2000));
  }
}
