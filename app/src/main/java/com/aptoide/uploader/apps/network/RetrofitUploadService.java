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
import com.aptoide.uploader.apps.UploadDraft;
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
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

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

  @Override
  public Single<UploadDraft> startUploadDraft(String md5, String language, String storeName,
      InstalledApp installedApp) {
    return Single.just(new UploadDraft(UploadDraft.Status.START, installedApp, md5));
  }

  @Override public Observable<UploadDraft> createDraft(String md5, InstalledApp installedApp) {
    return accountProvider.getToken()
        .flatMapObservable(
            accessToken -> serviceV3.createDraft(getParams(accessToken, md5, installedApp))
                .map(response -> mapCreateDraftResponse(response, installedApp, md5))
                .onErrorReturn(
                    throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR, installedApp,
                        md5)));
  }

  @Override
  public Observable<UploadDraft> setDraftStatus(UploadDraft draft, DraftStatus draftStatus) {
    return accountProvider.getToken()
        .flatMapObservable(
            accessToken -> serviceV3.setStatus(accessToken, String.valueOf(draft.getDraftId()),
                draftStatus.toString())
                .map(response -> mapSetDraftStatusResponse(response, draft.getInstalledApp(),
                    draft.getMd5(), draft.getDraftId()))
                .onErrorReturn(throwable -> {
                  throwable.printStackTrace();
                  return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
                      draft.getMd5());
                }));
  }

  @Override public Observable<UploadDraft> getDraftStatus(UploadDraft draft) {
    return accountProvider.getToken()
        .flatMapObservable(
            accessToken -> serviceV3.getStatus(accessToken, String.valueOf(draft.getDraftId()))
                .flatMap(response -> {
                  if (response != null && response.body()
                      .getData()
                      .getStatus()
                      .equals("PROCESSING")) {
                    throw new IOException();
                  }
                  uploaderAnalytics.sendUploadCompleteEvent("success", "Check if in Store", null,
                      null);
                  return Observable.just(
                      buildUploadFinishStatus(response, draft.getInstalledApp(), draft.getMd5()));
                })
                .retryWhen(new RetryWithDelay(6))
                .onErrorReturn(throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR,
                    draft.getInstalledApp(), draft.getMd5())));
  }

  @Override public Single<Boolean> hasApplicationMetaData(int draftId) {
    return accountProvider.getToken()
        .flatMap(accessToken -> serviceV3.hasApplicationMetaData(
            getParamsMetadataExists(accessToken, draftId))
            .map(result -> result.isSuccessful() && result.body() != null && result.body()
                .hasMetaData())
            .single(false));
  }

  @Override
  public Observable<Upload> upload(InstalledApp installedApp, String md5, String storeName,
      String apkPath) {
    return Observable.empty();
    //return accountProvider.getAccount()
    //    .firstOrError();
    //.flatMapObservable(aptoideAccount -> accountProvider.getToken()
    //.toObservable()
    //.flatMap(accessToken -> serviceV3.uploadAppToRepo(
    //    getParams(accessToken, aptoideAccount.getStoreName(), installedApp, false),
    //    MultipartBody.Part.createFormData("apk", apkPath,
    //        createFileRequestBody("apk", apkPath, installedApp.getPackageName())))
    //    .flatMap(response -> Observable.just(
    //        buildUploadFinishStatus(response, installedApp, md5)))
    //    .onErrorReturn(
    //        throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR, installedApp, md5))));
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

  @NonNull private UploadDraft mapCreateDraftResponse(Response<GenericDraftResponse> response,
      InstalledApp installedApp, String md5) {
    if (response.body()
        .getInfo()
        .getStatus()
        .equals(Status.FAIL)) {
      return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, installedApp, md5);
    }
    UploadDraft uploadDraft = new UploadDraft(UploadDraft.Status.PENDING, installedApp, md5);
    uploadDraft.setDraftId(response.body().data.getDraftId());
    return uploadDraft;
  }

  @NonNull private UploadDraft mapSetDraftStatusResponse(Response<GenericDraftResponse> response,
      InstalledApp installedApp, String md5, int draftId) {
    if (response.body()
        .getInfo()
        .getStatus()
        .equals(Status.FAIL)) {
      return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, installedApp, md5);
    }
    UploadDraft uploadDraft = new UploadDraft(UploadDraft.Status.STATUS_SET, installedApp, md5);
    uploadDraft.setDraftId(draftId);
    return uploadDraft;
  }

  @NonNull private UploadDraft buildUploadFinishStatus(Response<GenericDraftResponse> response,
      InstalledApp installedApp, String md5) {
    if (response.body()
        .getInfo()
        .getStatus()
        .equals(Status.ERROR)) {
      switch (response.body()
          .getError()
          .getCode()) {
        case "APK-103":
          return new UploadDraft(UploadDraft.Status.DUPLICATE, installedApp, md5);
        case "APK-5":
          return new UploadDraft(UploadDraft.Status.NOT_EXISTENT, installedApp, md5);
        case "APK-101":
          return new UploadDraft(UploadDraft.Status.INTELLECTUAL_RIGHTS, installedApp, md5);
        case "APK-102":
          return new UploadDraft(UploadDraft.Status.INFECTED, installedApp, md5);
        case "APK-106":
          return new UploadDraft(UploadDraft.Status.INVALID_SIGNATURE, installedApp, md5);
        case "APK-104":
          return new UploadDraft(UploadDraft.Status.PUBLISHER_ONLY, installedApp, md5);
        case "FILE-112":
          return new UploadDraft(UploadDraft.Status.APP_BUNDLE, installedApp, md5);
        default:
          return new UploadDraft(UploadDraft.Status.FAILED, installedApp, md5);
      }
    }
    return new UploadDraft(UploadDraft.Status.COMPLETED, installedApp, md5);
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

  @NonNull private Map<String, okhttp3.RequestBody> getParams(String token, String md5,
      InstalledApp installedApp) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), token));
    parameters.put("apk_md5sum", RequestBody.create(MediaType.parse("text/plain"), md5));
    parameters.put("package_name",
        RequestBody.create(MediaType.parse("text/plain"), installedApp.getPackageName()));
    parameters.put("vercode", RequestBody.create(MediaType.parse("text/plain"),
        String.valueOf(installedApp.getVersionCode())));
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

  @NonNull
  private Map<String, okhttp3.RequestBody> getParamsMetadataExists(String token, int draftId) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), token));
    parameters.put("draft_id",
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(draftId)));
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
    @Multipart @POST("7/uploader/draft/create")
    Observable<Response<GenericDraftResponse>> createDraft(
        @PartMap Map<String, okhttp3.RequestBody> params);

    @GET("7/uploader/draft/status/set") Observable<Response<GenericDraftResponse>> setStatus(
        @Query("access_token") String accessToken, @Query("draft_id") String draftId,
        @Query("status") String status);

    @GET("7/uploader/draft/status/get") Observable<Response<GenericDraftResponse>> getStatus(
        @Query("access_token") String accessToken, @Query("draft_id") String draftId);

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

    @POST("7/uploader/draft/metadata/exists") @FormUrlEncoded
    Observable<Response<HasApplicationMetaDataResponse>> hasApplicationMetaData(
        @PartMap Map<String, okhttp3.RequestBody> params);
  }
}
