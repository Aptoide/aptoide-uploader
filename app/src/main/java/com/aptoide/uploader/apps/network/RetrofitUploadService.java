package com.aptoide.uploader.apps.network;

import android.support.annotation.NonNull;
import com.aptoide.uploader.account.network.Status;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.upload.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public class RetrofitUploadService implements UploaderService {

  private final ServiceV7 serviceV7;
  private final ServiceV3 serviceV3;
  private final AccountProvider accountProvider;

  public RetrofitUploadService(ServiceV7 serviceV7, ServiceV3 serviceV3,
      AccountProvider accountProvider) {
    this.serviceV7 = serviceV7;
    this.serviceV3 = serviceV3;
    this.accountProvider = accountProvider;
  }

  @Override public Single<Upload> getUpload(String md5, String language, String storeName,
      InstalledApp installedApp) {
    return serviceV7.getProposed(installedApp.getPackageName(), language, false)
        .singleOrError()
        .flatMap(response -> {
          final GetProposedResponse proposedBody = response.body();

          if ((response.isSuccessful() && proposedBody != null) && (proposedBody.getInfo()
              .getStatus()
              .equals(Status.FAIL) || proposedBody.getData()
              .isEmpty())) {
            return Single.just(
                new Upload(false, false, installedApp, Upload.Status.PENDING, md5, storeName));
          }
          return Single.error(new IllegalStateException(response.message()));
        });
  }

  @Override public Observable<Upload> upload(String md5, String storeName, String installedAppName,
      boolean hasProposedData, InstalledApp installedApp) {
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> serviceV3.uploadAppToRepo(
            getParams(accessToken, md5, storeName, installedAppName))
            .map(response -> buildUploadFinishStatus(response, hasProposedData, installedApp, md5,
                storeName))
            .startWith(buildUploadProgressStatus(hasProposedData, installedApp, md5, storeName))
            .doOnError(throwable -> throwable.printStackTrace())
            .onErrorReturn(throwable -> new Upload(false, hasProposedData, installedApp,
                Upload.Status.CLIENT_ERROR, md5, storeName)));
  }

  @Override public Single<Upload> hasApplicationMetaData(String md5) {
    // TODO: 5/11/18 implement this request
    return null;
  }

  private Upload buildUploadProgressStatus(boolean proposedData, InstalledApp installedApp,
      String md5, String storeName) {
    return new Upload(false, proposedData, installedApp, Upload.Status.PROGRESS, md5, storeName);
  }

  @NonNull private Upload buildUploadFinishStatus(Response<UploadAppToRepoResponse> response,
      boolean hasProposedData, InstalledApp installedApp, String md5, String storeName) {
    if (response.body()
        .getStatus()
        .equals(Status.FAIL)) {
      if (response.body()
          .getErrors()
          .get(0)
          .getCode()
          .equals("APK-103")) {
        return new Upload(response.isSuccessful(), hasProposedData, installedApp,
            Upload.Status.DUPLICATE, md5, storeName);
      } else if (response.body()
          .getErrors()
          .get(0)
          .getCode()
          .equals("APK-5")) {
        return new Upload(response.isSuccessful(), hasProposedData, installedApp,
            Upload.Status.NOT_EXISTENT, md5, storeName);
      }
    }
    return new Upload(response.isSuccessful(), hasProposedData, installedApp,
        Upload.Status.COMPLETED, md5, storeName);
  }

  @NonNull
  private Map<String, okhttp3.RequestBody> getParams(String token, String md5, String storeName,
      String installedAppName) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), token));
    parameters.put("repo", RequestBody.create(MediaType.parse("text/plain"), storeName));
    parameters.put("apkname", RequestBody.create(MediaType.parse("text/plain"), installedAppName));
    parameters.put("apk_md5sum", RequestBody.create(MediaType.parse("text/plain"), md5));
    parameters.put("mode", RequestBody.create(MediaType.parse("text/plain"), "json"));
    parameters.put("repo", RequestBody.create(MediaType.parse("text/plain"), storeName));
    parameters.put("uploadType", RequestBody.create(MediaType.parse("text/plain"), "aptuploader"));
    return parameters;
  }

  public interface ServiceV7 {
    @GET("api/7/apks/package/translations/getProposed/package_name/{packageName}/language_code/{languageCode}/filter/{filter}")
    Observable<Response<GetProposedResponse>> getProposed(@Path("packageName") String packageName,
        @Path("languageCode") String languageCode, @Path("filter") boolean filter);
  }

  public interface ServiceV3 {
    @Multipart @POST("3/uploadAppToRepo")
    Observable<Response<UploadAppToRepoResponse>> uploadAppToRepo(
        @PartMap Map<String, okhttp3.RequestBody> params);

    @Multipart @POST("3/hasApplicationMetaData")
    Observable<Response<HasApplicationMetaDataResponse>> hasApplicationMetaData(
        @PartMap Map<String, okhttp3.RequestBody> params);
  }
}
