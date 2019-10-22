package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.App;
import com.aptoide.uploader.upload.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class RetrofitStoreService {

  private final RetrofitStoreService.ServiceV7 serviceV7;
  private final AccountProvider accountProvider;

  public RetrofitStoreService(RetrofitStoreService.ServiceV7 serviceV7,
      AccountProvider accountProvider) {
    this.serviceV7 = serviceV7;
    this.accountProvider = accountProvider;
  }

  public Observable<ApksResponse> getApks(List<String> md5List, String storeName) {
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> serviceV7.getApks(md5List, storeName, accessToken)
            .map(getApksResponse -> {
              if (getApksResponse.isSuccessful()) {
                return mapToApksList(getApksResponse.body(), ApksResponse.Status.OK);
              } else {
                return new ApksResponse(new ApksResponse.Errors(getApksResponse.body()
                    .getError()
                    .getCode(), getApksResponse.body()
                    .getError()
                    .getDescription()), ApksResponse.Status.FAIL);
              }
            })
            .onErrorReturn(
                throwable -> new ApksResponse(new ApksResponse.Errors("error", "server error"),
                    ApksResponse.Status.FAIL))
            .subscribeOn(Schedulers.io()));
  }

  private ApksResponse mapToApksList(GetApksResponse getApksResponse, ApksResponse.Status status) {
    List<App> list = new ArrayList<>();
    ApksResponse apksResponse = new ApksResponse(list, status);
    for (GetApksResponse.Data apk : getApksResponse.getDatalist()
        .getList()) {
      list.add(new App(apk.getId(), apk.getName(), apk.getSize(), apk.getIcon(), apk.getGraphic(),
          apk.getStatus(), apk.getMode(), apk.getAdded(), apk.getModified(), apk.getUpdated(),
          apk.getPackage(), apk.getFile()));
    }
    return apksResponse;
  }

  public interface ServiceV7 {
    @POST("/api/7/my/apps/apks/get") @FormUrlEncoded Observable<Response<GetApksResponse>> getApks(
        @Field("apk_md5sums") List<String> md5List, @Field("store_name") String storeName,
        @Field("access_token") String accessToken);
  }
}