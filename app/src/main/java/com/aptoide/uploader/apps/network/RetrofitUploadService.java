package com.aptoide.uploader.apps.network;

import android.support.annotation.NonNull;
import com.aptoide.uploader.account.AptoideAccount;
import com.aptoide.uploader.account.network.ResponseV7;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Md5Calculator;
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
  private final Md5Calculator md5Calculator;

  public RetrofitUploadService(ServiceV7 serviceV7, ServiceV3 serviceV3,
      AccountProvider accountProvider, Md5Calculator md5Calculator) {
    this.serviceV7 = serviceV7;
    this.serviceV3 = serviceV3;
    this.accountProvider = accountProvider;
    this.md5Calculator = md5Calculator;
  }

  @Override public Single<Upload> getAppUpload(String md5, String language, String storeName,
      InstalledApp installedApp) {
    return serviceV7.getProposed(installedApp.getPackageName(), language, false)
        .singleOrError()
        .flatMap(response -> {
          final GetProposedResponse proposedBody = response.body();

          if (response.isSuccessful() && proposedBody != null && proposedBody.getInfo()
              .getStatus()
              .equals(ResponseV7.Info.Status.FAIL)) {
            return Single.just(new Upload(false, false, installedApp, Upload.Status.PENDING));
          }

          return Single.error(new IllegalStateException(response.message()));
        });
  }

  @Override public Observable<Upload> uploadAppToRepo(Upload upload) {
    return accountProvider.getAccount()
        .flatMapObservable(account -> accountProvider.getToken()
            .flatMapObservable(token -> md5Calculator.calculate(upload.getInstalledApp())
                .flatMapObservable(
                    md5 -> serviceV3.uploadAppToRepo(getParams(account, upload, token, md5))
                        .map(response -> buildUploadFinishStatus(response, upload))
                        .startWith(buildUploadProgressStatus(upload)))));
  }

  private Upload buildUploadProgressStatus(Upload upload) {
    return new Upload(false, upload.hasProposedData(), upload.getInstalledApp(),
        Upload.Status.PROGRESS);
  }

  @NonNull private Upload buildUploadFinishStatus(Response<UploadAppToRepoResponse> response,
      Upload upload) {
    return new Upload(response.isSuccessful(), upload.hasProposedData(), upload.getInstalledApp(),
        Upload.Status.COMPLETED);
  }

  @NonNull private Map<String, okhttp3.RequestBody> getParams(AptoideAccount account, Upload upload,
      String token, String md5) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), token));
    parameters.put("repo",
        RequestBody.create(MediaType.parse("text/plain"), account.getStoreName()));
    InstalledApp installedApp = upload.getInstalledApp();
    parameters.put("apkname",
        RequestBody.create(MediaType.parse("text/plain"), installedApp.getName()));
    parameters.put("apk_md5sum", RequestBody.create(MediaType.parse("text/plain"), md5));
    parameters.put("mode", RequestBody.create(MediaType.parse("text/plain"), "json"));
    parameters.put("repo",
        RequestBody.create(MediaType.parse("text/plain"), account.getStoreName()));
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
  }
}
