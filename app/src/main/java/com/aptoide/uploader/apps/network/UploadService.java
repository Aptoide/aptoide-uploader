package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Upload;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Map;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public class UploadService implements UploaderService {

  private ServiceV7 serviceV7;
  private ServiceV3 serviceV3;

  public UploadService(ServiceV7 serviceV7, ServiceV3 serviceV3) {
    this.serviceV7 = serviceV7;
    this.serviceV3 = serviceV3;
  }

  @Override public Single<Upload> getAppUpload(String md5, String language, String storeName,
      InstalledApp installedApp) {
    return serviceV7.getProposed(
        new GetProposedRequestBody(language, installedApp.getPackageName(), false))
        .singleOrError()
        .flatMap(response -> {
          final GetProposedResponse proposedBody = response.body();

          if (response.isSuccessful() && proposedBody != null && proposedBody.isOk()) {
            return Single.just(new Upload(false, false, installedApp, Upload.Status.PENDING));
          }

          return Single.error(new IllegalStateException(response.message()));
        });
  }

  @Override public Observable<Void> uploadAppToRepo(Upload upload) {
    return Observable.empty();
  }

  public interface ServiceV7 {
    @POST("/7/apks/package/translations/getProposed") @FormUrlEncoded
    Observable<Response<GetProposedResponse>> getProposed(@Body GetProposedRequestBody body);
  }

  public interface ServiceV3 {
    @Multipart @POST("/3/uploadAppToRepo")
    Observable<Response<UploadAppToRepoResponse>> uploadAppToRepo(
        @PartMap Map<String, Object> params);
  }
}
