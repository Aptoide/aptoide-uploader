package com.aptoide.uploader.apps.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Upload;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class UploadService extends Service implements UploaderService {

  private ServiceV7 serviceV7;

  public UploadService(ServiceV7 serviceV7) {
    this.serviceV7 = serviceV7;
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public Single<Upload> getAppUpload(String md5, String language, String storeName,
      InstalledApp installedApp) {
    final HashMap<String, String> parameters = new HashMap<>();
    parameters.put("language_code", language);
    parameters.put("package_name", installedApp.getPackageName());
    // TODO: 26-12-2017 filipe  confirm filter value
    parameters.put("filter", Boolean.toString(false));
    return serviceV7.getProposed(parameters)
        .singleOrError()
        .flatMap(response -> {
          if (response.isSuccessful() && response.body() != null && !response.body()
              .hasErrors()) {
            if (response.body()
                .requestFailed()) {
              // TODO: 26-12-2017 filipe uploadAppToRepo
              return Single.just(new Upload(false, false, installedApp, Upload.Status.PENDING));
            } else {
              List<GetProposedResponse.Data> dataList = response.body()
                  .getData();
              if (!dataList.isEmpty()) {
                GetProposedResponse.Data proposedData = dataList.get(0);
                // TODO: 26-12-2017 filipe request category with getApkInfo
              }
            }
          } else if (response.body()
              .hasErrors()) {
            return Single.error(new IllegalStateException(response.message()));
          }
          return Single.error(new IllegalStateException(response.message()));
        });
  }

  @Override public void uploadAppToRepo() {

  }

  public interface ServiceV7 {
    @POST("/apks/package/translations/getProposed") @FormUrlEncoded
    Observable<Response<GetProposedResponse>> getProposed(@FieldMap HashMap<String, String> args);
  }
}
