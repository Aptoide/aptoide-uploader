package com.aptoide.uploader.apps.network;

import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.aptoide.uploader.account.network.Status;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.apps.MetadataUpload;
import com.aptoide.uploader.apps.Upload;
import com.aptoide.uploader.apps.UploadProgressListener;
import com.aptoide.uploader.upload.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public class RetrofitUploadService implements UploaderService {

  private static final String RESPONSE_MODE = "json";
  private final ServiceV3 serviceV3;
  private final AccountProvider accountProvider;
  private final UploadType uploadType;
  private UploadProgressListener uploadProgressListener;
  private UploaderAnalytics uploaderAnalytics;

  public RetrofitUploadService(ServiceV3 serviceV3, AccountProvider accountProvider,
      UploadType uploadType, UploadProgressListener uploadProgressListener,
      UploaderAnalytics uploaderAnalytics) {
    this.serviceV3 = serviceV3;
    this.accountProvider = accountProvider;
    this.uploadType = uploadType;
    this.uploadProgressListener = uploadProgressListener;
    this.uploaderAnalytics = uploaderAnalytics;
  }

  @Override public Single<Upload> getUpload(String md5, String language, String storeName,
      InstalledApp installedApp) {
    return Single.just(new Upload(false, installedApp, Upload.Status.PENDING, md5, storeName));
  }

  @Override public Observable<Upload> upload(String md5, String storeName, String installedAppName,
      InstalledApp installedApp) {
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> serviceV3.uploadAppToRepo(
            getParams(accessToken, md5, storeName, installedAppName, installedApp))
            .map(response -> buildUploadFinishStatus(response, installedApp, md5, storeName))
            .startWith(buildUploadProgressStatus(installedApp, md5, storeName))
            .onErrorReturn(
                throwable -> new Upload(false, installedApp, Upload.Status.CLIENT_ERROR, md5,
                    storeName)));
  }

  @Override public Single<Boolean> hasApplicationMetaData(String packageName, int versionCode) {
    return serviceV3.hasApplicationMetaData(packageName, versionCode, RESPONSE_MODE)
        .map(result -> result.isSuccessful() && result.body() != null && result.body()
            .hasMetaData())
        .single(false);
  }

  @Override
  public Observable<Upload> upload(InstalledApp installedApp, String md5, String storeName,
      String apkPath) {
    return accountProvider.getAccount()
        .firstOrError()
        .flatMapObservable(aptoideAccount -> accountProvider.getToken()
            .toObservable()
            .flatMap(accessToken -> serviceV3.uploadAppToRepo(
                getParams(accessToken, aptoideAccount.getStoreName(), installedApp, false),
                MultipartBody.Part.createFormData("apk", apkPath,
                    createFileRequestBody("apk", apkPath, installedApp.getPackageName())))
                .flatMap(response -> Observable.just(
                    buildUploadFinishStatus(response, installedApp, md5, storeName)))
                .onErrorReturn(
                    throwable -> new Upload(false, installedApp, Upload.Status.CLIENT_ERROR, md5,
                        storeName))));
  }

  //@Override
  //public Observable<Upload> upload(InstalledApp installedApp, String md5, String storeName,
  //    String obbPath, String obbType) {
  //  return accountProvider.getAccount()
  //      .firstOrError()
  //      .flatMapObservable(aptoideAccount -> accountProvider.getToken()
  //          .toObservable()
  //          .flatMap(accessToken -> serviceV3.uploadAppToRepo(
  //              getParams(accessToken, aptoideAccount.getStoreName(), installedApp, true),
  //              MultipartBody.Part.createFormData(obbType, obbPath,
  //                  createFileRequestBody("obb", obbPath, installedApp.getPackageName())))
  //              .flatMap(response -> Observable.just(
  //                  buildUploadFinishStatus(response, installedApp, md5, storeName)))
  //              .onErrorReturn(
  //                  throwable -> new Upload(false, installedApp, Upload.Status.CLIENT_ERROR, md5,
  //                      storeName))));
  //}

  @Override public Observable<Upload> upload(String md5, String storeName, String appName,
      InstalledApp installedApp, Metadata metadata) {
    return accountProvider.getAccount()
        .firstOrError()
        .flatMapObservable(aptoideAccount -> accountProvider.getToken()
            .toObservable()
            .flatMap(accessToken -> serviceV3.uploadAppToRepo(
                getParams(accessToken, aptoideAccount.getStoreName(), metadata, installedApp,
                    false), MultipartBody.Part.createFormData("apk", installedApp.getApkPath(),
                    createFileRequestBody("apk", installedApp.getApkPath(),
                        installedApp.getPackageName())))
                .flatMap(response -> Observable.just(
                    buildUploadFinishStatus(response, installedApp, md5, storeName, metadata)))
                .onErrorReturn(
                    throwable -> new MetadataUpload(false, installedApp, Upload.Status.CLIENT_ERROR,
                        md5, storeName, metadata))));
  }

  //@Override public Observable<Upload> upload(String md5, String storeName, String appName,
  //    InstalledApp installedApp, Metadata metadata, String obbMainPath) {
  //  return accountProvider.getAccount()
  //      .firstOrError()
  //      .flatMapObservable(aptoideAccount -> accountProvider.getToken()
  //          .toObservable()
  //          .flatMap(accessToken -> serviceV3.uploadAppToRepo(
  //              getParams(accessToken, aptoideAccount.getStoreName(), metadata, installedApp, true),
  //              MultipartBody.Part.createFormData("apk", installedApp.getApkPath(),
  //                  createFileRequestBody("apk", installedApp.getApkPath(),
  //                      installedApp.getPackageName())),
  //              MultipartBody.Part.createFormData("obb_main", obbMainPath,
  //                  createFileRequestBody("obb", obbMainPath, installedApp.getPackageName())))
  //              .flatMap(response -> Observable.just(
  //                  buildUploadFinishStatus(response, installedApp, md5, storeName, metadata)))
  //              .onErrorReturn(
  //                  throwable -> new Upload(false, installedApp, Upload.Status.CLIENT_ERROR, md5,
  //                      storeName))));
  //}

  private Map<String, RequestBody> getParams(String accessToken, String storeName,
      InstalledApp installedApp, boolean obbFileFlag) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("rating", RequestBody.create(MediaType.parse("text/plain"), "0"));
    parameters.put("category", RequestBody.create(MediaType.parse("text/plain"), "0"));
    parameters.put("only_user_repo", RequestBody.create(MediaType.parse("text/plain"), "false"));
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), accessToken));
    parameters.put("repo", RequestBody.create(MediaType.parse("text/plain"), storeName));
    parameters.put("mode", RequestBody.create(MediaType.parse("text/plain"), RESPONSE_MODE));
    parameters.put("uploadType",
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(uploadType.getType())));

    //if (installedApp.getObbMainPath() != null) {
    //  if (!obbFileFlag) {
    //    parameters.put("obb_main_md5sum",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbMainMd5()));
    //    parameters.put("obb_main_filename",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbMainFilename()));
    //  }
    //}
    //if (installedApp.getObbPatchPath() != null) {
    //  if (!obbFileFlag) {
    //    parameters.put("obb_patch_md5sum",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbPatchMd5()));
    //    parameters.put("obb_patch_filename",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbPatchFilename()));
    //  }
    //}
    Log.w(getClass().getSimpleName(), parameters.toString());
    return parameters;
  }

  private Map<String, RequestBody> getParams(String accessToken, String storeName,
      Metadata metadata, InstalledApp installedApp, boolean obbFileFlag) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), accessToken));
    parameters.put("repo", RequestBody.create(MediaType.parse("text/plain"), storeName));
    parameters.put("only_user_repo", RequestBody.create(MediaType.parse("text/plain"), "false"));
    parameters.put("uploadType",
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(uploadType.getType())));
    parameters.put("mode", RequestBody.create(MediaType.parse("text/plain"), RESPONSE_MODE));
    parameters.put("apkname",
        RequestBody.create(MediaType.parse("text/plain"), metadata.getName()));
    parameters.put("description",
        RequestBody.create(MediaType.parse("text/plain"), metadata.getDescription()));
    parameters.put("category",
        RequestBody.create(MediaType.parse("text/plain"), metadata.getCategory()));
    parameters.put("rating",
        RequestBody.create(MediaType.parse("text/plain"), metadata.getAgeRating()));
    parameters.put("lang", RequestBody.create(MediaType.parse("text/plain"), metadata.getLang()));
    if (metadata.getPhoneNumber() != null) {
      parameters.put("apk_phone",
          RequestBody.create(MediaType.parse("text/plain"), metadata.getPhoneNumber()));
    }
    if (metadata.getEmail() != null) {
      parameters.put("apk_email",
          RequestBody.create(MediaType.parse("text/plain"), metadata.getEmail()));
    }
    if (metadata.getWebsite() != null) {
      parameters.put("apk_website",
          RequestBody.create(MediaType.parse("text/plain"), metadata.getWebsite()));
    }
    //if (installedApp.getObbMainPath() != null) {
    //  if (!obbFileFlag) {
    //    parameters.put("obb_main_md5sum",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbMainMd5()));
    //    parameters.put("obb_main_filename",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbMainFilename()));
    //  }
    //}
    //if (installedApp.getObbPatchPath() != null) {
    //  if (!obbFileFlag) {
    //
    //    parameters.put("obb_patch_md5sum",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbPatchMd5()));
    //    parameters.put("obb_patch_filename",
    //        RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbPatchFilename()));
    //  }
    //}
    return parameters;
  }

  @NonNull
  private RequestBody createFileRequestBody(String extension, String apkPath, String packageName) {
    return new RequestBody() {
      @Override public MediaType contentType() {
        String mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension);
        if (mimeType == null) mimeType = "application/octet-stream";
        return MediaType.parse(mimeType);
      }

      @Override public void writeTo(BufferedSink sink) throws IOException {
        byte[] buffer = new byte[4096];
        File apk = new File(apkPath);
        FileInputStream in = new FileInputStream(apk);

        long fileSize = apk.length();

        long percentageTicks = fileSize / 1024 / 100;

        int parts = 0;
        int progress = 0;

        try {
          int read = in.read(buffer);
          while (read != -1) {
            sink.write(buffer, 0, read);
            read = in.read(buffer);
            parts++;
            if (percentageTicks > 0 && parts % percentageTicks == 0) {
              progress = (int) (parts * (double) buffer.length / fileSize * 100.0);
              uploadProgressListener.updateProgress(progress, packageName);
            }
          }
          if (read == -1) {
            uploadProgressListener.updateProgress(100, packageName);
          }
        } finally {
          in.close();
        }
      }
    };
  }

  private Upload buildUploadProgressStatus(InstalledApp installedApp, String md5,
      String storeName) {
    return new Upload(false, installedApp, Upload.Status.PROGRESS, md5, storeName);
  }

  @NonNull private Upload buildUploadFinishStatus(Response<UploadAppToRepoResponse> response,
      InstalledApp installedApp, String md5, String storeName) {
    if (response.body()
        .getStatus()
        .equals(Status.FAIL)) {
      switch (response.body()
          .getErrors()
          .get(0)
          .getCode()) {
        case "APK-103":
          sendAnalytics("fail", response);
          return new Upload(response.isSuccessful(), installedApp, Upload.Status.DUPLICATE, md5,
              storeName);
        case "APK-5":
          sendAnalytics("fail", response);
          return new Upload(response.isSuccessful(), installedApp, Upload.Status.NOT_EXISTENT, md5,
              storeName);
        case "APK-101":
          sendAnalytics("fail", response);
          return new Upload(response.isSuccessful(), installedApp,
              Upload.Status.INTELLECTUAL_RIGHTS, md5, storeName);
        case "APK-102":
          sendAnalytics("fail", response);
          return new Upload(response.isSuccessful(), installedApp, Upload.Status.INFECTED, md5,
              storeName);
        case "APK-106":
          sendAnalytics("fail", response);
          return new Upload(response.isSuccessful(), installedApp, Upload.Status.INVALID_SIGNATURE,
              md5, storeName);
        case "APK-104":
          sendAnalytics("fail", response);
          return new Upload(response.isSuccessful(), installedApp, Upload.Status.PUBLISHER_ONLY,
              md5, storeName);
        case "FILE-112":
          sendAnalytics("fail", response);
          return new Upload(response.isSuccessful(), installedApp, Upload.Status.APP_BUNDLE, md5,
              storeName);
        default:
          if (!response.body()
              .getErrors()
              .get(0)
              .equals("APK-5")) {
            sendAnalytics("fail", response);
          }
          return new Upload(response.isSuccessful(), installedApp, Upload.Status.FAILED, md5,
              storeName);
      }
    }
    uploaderAnalytics.sendUploadCompleteEvent("success", "Upload App to Repo", "0", "0");
    return new Upload(response.isSuccessful(), installedApp, Upload.Status.COMPLETED, md5,
        storeName);
  }

  @NonNull private Upload buildUploadFinishStatus(Response<UploadAppToRepoResponse> response,
      InstalledApp installedApp, String md5, String storeName, Metadata metadata) {
    if (response.body()
        .getStatus()
        .equals(Status.FAIL)) {
      switch (response.body()
          .getErrors()
          .get(0)
          .getCode()) {
        case "APK-103":
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp, Upload.Status.DUPLICATE,
              md5, storeName, metadata);
        case "APK-5":
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp,
              Upload.Status.NOT_EXISTENT, md5, storeName, metadata);
        case "APK-101":
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp,
              Upload.Status.INTELLECTUAL_RIGHTS, md5, storeName, metadata);
        case "APK-102":
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp, Upload.Status.INFECTED,
              md5, storeName, metadata);
        case "APK-106":
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp,
              Upload.Status.INVALID_SIGNATURE, md5, storeName, metadata);
        case "APK-104":
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp,
              Upload.Status.PUBLISHER_ONLY, md5, storeName, metadata);
        case "FILE-112":
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp, Upload.Status.APP_BUNDLE,
              md5, storeName, metadata);
        default:
          sendAnalytics("fail", response);
          return new MetadataUpload(response.isSuccessful(), installedApp, Upload.Status.RETRY, md5,
              storeName, metadata);
      }
    }
    uploaderAnalytics.sendUploadCompleteEvent("success", "Upload App to Repo", null, null);
    return new MetadataUpload(response.isSuccessful(), installedApp, Upload.Status.COMPLETED, md5,
        storeName, metadata);
  }

  @NonNull
  private Map<String, okhttp3.RequestBody> getParams(String token, String md5, String storeName,
      String installedAppName, InstalledApp installedApp) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), token));
    parameters.put("apkname", RequestBody.create(MediaType.parse("text/plain"), installedAppName));
    parameters.put("apk_md5sum", RequestBody.create(MediaType.parse("text/plain"), md5));
    parameters.put("mode", RequestBody.create(MediaType.parse("text/plain"), "json"));
    parameters.put("repo", RequestBody.create(MediaType.parse("text/plain"), storeName));
    parameters.put("uploadType", RequestBody.create(MediaType.parse("text/plain"), "aptuploader"));
    //if (installedApp.getObbMainPath() != null) {
    //  parameters.put("obb_main_md5sum",
    //      RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbMainMd5()));
    //  parameters.put("obb_main_filename",
    //      RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbMainFilename()));
    //}
    //if (installedApp.getObbPatchPath() != null) {
    //  parameters.put("obb_patch_md5sum",
    //      RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbPatchMd5()));
    //  parameters.put("obb_patch_filename",
    //      RequestBody.create(MediaType.parse("text/plain"), installedApp.getObbPatchFilename()));
    //}
    Log.w(getClass().getSimpleName(), parameters.toString());
    return parameters;
  }

  private void sendAnalytics(String status, Response<UploadAppToRepoResponse> response) {
    uploaderAnalytics.sendUploadCompleteEvent(status, "Upload App to Repo", response.body()
        .getErrors()
        .get(0)
        .getCode(), response.body()
        .getErrors()
        .get(0)
        .getMsg());
  }

  public enum UploadType {
    WEBSERVICE(1), APTOIDE_UPLOADER(2), DROPBOX(3), APTOIDE_BACKUP(4);

    private final int type;

    UploadType(int type) {
      this.type = type;
    }

    public int getType() {
      return type;
    }
  }

  public interface ServiceV3 {
    @Multipart @POST("3/uploadAppToRepo")
    Observable<Response<UploadAppToRepoResponse>> uploadAppToRepo(
        @PartMap Map<String, okhttp3.RequestBody> params);

    @Multipart @POST("3/uploadAppToRepo")
    Observable<Response<UploadAppToRepoResponse>> uploadAppToRepo(
        @PartMap Map<String, okhttp3.RequestBody> params, @Part MultipartBody.Part apkFile);

    @Multipart @POST("3/uploadAppToRepo")
    Observable<Response<UploadAppToRepoResponse>> uploadAppToRepo(
        @PartMap Map<String, okhttp3.RequestBody> params, @Part MultipartBody.Part apkFile,
        @Part MultipartBody.Part obbMain, @Part MultipartBody.Part obbPatch);

    @Multipart @POST("3/uploadAppToRepo")
    Observable<Response<UploadAppToRepoResponse>> uploadAppToRepo(
        @PartMap Map<String, okhttp3.RequestBody> params, @Part MultipartBody.Part apkFile,
        @Part MultipartBody.Part obbMain);

    @POST("3/hasApplicationMetaData") @FormUrlEncoded
    Observable<Response<HasApplicationMetaDataResponse>> hasApplicationMetaData(
        @Field("package") String packageName, @Field("vercode") int versionCode,
        @Field("mode") String responseMode);
  }
}
