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

  public Observable<List<AppUploadStatus>> getApks(List<AppUploadStatus> uploadStatuses,
      String storeName) {
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> {
          ArrayList<String> newList = new ArrayList<>();
          for (AppUploadStatus status : uploadStatuses) {
            newList.add(status.getMd5());
          }
          return serviceV7.getApks(new GetApksRequest(newList, storeName, accessToken))
              .map(getApksResponse -> {
                if (getApksResponse.isSuccessful()) {
                  return mapToApksList(getApksResponse.body(), uploadStatuses);
                } else {
                  return new ArrayList<AppUploadStatus>();
                }
              })
              .subscribeOn(Schedulers.io());
        });
  }

  private List<AppUploadStatus> mapToApksList(GetApksResponse getApksResponse,
      List<AppUploadStatus> uploadStatuses) {
    List<AppUploadStatus> appUploadStatusList = new ArrayList<>();

    for (AppUploadStatus status : uploadStatuses) {
      appUploadStatusList.add(new AppUploadStatus(status.getMd5(), status.getPackageName(),
          AppUploadStatus.Status.NOT_IN_STORE, status.getVercode()));
    }
    for (GetApksResponse.Data apk : getApksResponse.getDatalist()
        .getList()) {
      appUploadStatusList.set(appUploadStatusList.indexOf(new AppUploadStatus(apk.getFile()
          .getMd5sum(), apk.getFile()
          .getaPackage()
          .getName(), AppUploadStatus.Status.NOT_IN_STORE, apk.getFile()
          .getVercode())), new AppUploadStatus(apk.getFile()
          .getMd5sum(), apk.getFile()
          .getaPackage()
          .getName(), AppUploadStatus.Status.IN_STORE, apk.getFile()
          .getVercode()));
    }
    return appUploadStatusList;
  }

  public interface ServiceV7 {
    @POST("/api/7/my/apps/apks/get") Observable<Response<GetApksResponse>> getApks(
        @Body GetApksRequest body);
  }
}
