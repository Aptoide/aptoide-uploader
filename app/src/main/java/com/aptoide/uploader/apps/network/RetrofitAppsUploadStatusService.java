package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.AppUploadStatus;
import com.aptoide.uploader.upload.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class RetrofitAppsUploadStatusService {

  private final RetrofitAppsUploadStatusService.ServiceV7 serviceV7;
  private final AccountProvider accountProvider;

  public RetrofitAppsUploadStatusService(RetrofitAppsUploadStatusService.ServiceV7 serviceV7,
      AccountProvider accountProvider) {
    this.serviceV7 = serviceV7;
    this.accountProvider = accountProvider;
  }

  public Observable<List<AppUploadStatus>> getApks(List<String> md5List, String storeName) {
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> {
          ArrayList<String> newList = new ArrayList<>();
          newList.addAll(md5List);
          return serviceV7.getApks(new GetApksRequest(newList, storeName, accessToken))
              .map(getApksResponse -> {
                if (getApksResponse.isSuccessful()) {
                  return mapToApksList(getApksResponse.body());
                } else {
                  return new ArrayList<AppUploadStatus>();
                }
              })
              .subscribeOn(Schedulers.io());
        });
  }

  private List<AppUploadStatus> mapToApksList(GetApksResponse getApksResponse) {
    List<AppUploadStatus> appUploadStatusList = new ArrayList<>();
    for (GetApksResponse.Data apk : getApksResponse.getDatalist()
        .getList()) {
      appUploadStatusList.add(new AppUploadStatus(apk.getFile()
          .getMd5sum(), apk.getFile()
          .getaPackage()
          .getName(), true, apk.getFile()
          .getVercode()));
    }
    return appUploadStatusList;
  }

  public interface ServiceV7 {
    @POST("/api/7/my/apps/apks/get") Observable<Response<GetApksResponse>> getApks(
        @Body GetApksRequest body);
  }
}
