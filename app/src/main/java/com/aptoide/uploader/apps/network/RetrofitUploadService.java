package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.ResponseV7;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.security.AuthenticationProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Map;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public class RetrofitUploadService implements UploaderService {

  private final ServiceV7Secondary serviceV7Secondary;
  private final AuthenticationProvider authenticationProvider;
  private final ServiceV7 serviceV7;
  private final ServiceV3 serviceV3;

  public RetrofitUploadService(ServiceV7 serviceV7, ServiceV7Secondary serviceV7Secondary,
      ServiceV3 serviceV3, AuthenticationProvider authenticationProvider) {
    this.serviceV7 = serviceV7;
    this.serviceV7Secondary = serviceV7Secondary;
    this.serviceV3 = serviceV3;
    this.authenticationProvider = authenticationProvider;
  }

  @Override public Single<Upload> getAppUpload(String md5, String language, String storeName,
      InstalledApp installedApp) {
    return serviceV7Secondary.getProposed(installedApp.getPackageName(), language, false)
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

  @Override public Observable<Void> uploadAppToRepo(Upload upload) {
    return Observable.empty();
  }

  public interface ServiceV7 {

  }

  public interface ServiceV7Secondary {
    @GET("api/7/apks/package/translations/getProposed/package_name/{packageName}/language_code/{languageCode}/filter/{filter}")
    Observable<Response<GetProposedResponse>> getProposed(@Path("packageName") String packageName,
        @Path("languageCode") String languageCode, @Path("filter") boolean filter);
  }

  public interface ServiceV3 {
    @Multipart @POST("/3/uploadAppToRepo")
    Observable<Response<UploadAppToRepoResponse>> uploadAppToRepo(
        @PartMap Map<String, Object> params);
  }
}
