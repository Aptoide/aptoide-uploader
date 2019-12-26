package com.aptoide.uploader.apps.network;

import android.accounts.NetworkErrorException;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.aptoide.uploader.account.network.Status;
import com.aptoide.uploader.analytics.UploaderAnalytics;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.Md5Calculator;
import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.apps.UploadDraft;
import com.aptoide.uploader.apps.UploadProgressListener;
import com.aptoide.uploader.upload.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public class RetrofitUploadService implements UploaderService {

  private final ServiceV7 serviceV7;
  private final AccountProvider accountProvider;
  private UploadProgressListener uploadProgressListener;
  private UploaderAnalytics uploaderAnalytics;
  private Md5Calculator md5Calculator;

  public RetrofitUploadService(ServiceV7 serviceV7, AccountProvider accountProvider,
      UploadProgressListener uploadProgressListener, UploaderAnalytics uploaderAnalytics,
      Md5Calculator md5Calculator) {
    this.serviceV7 = serviceV7;
    this.accountProvider = accountProvider;
    this.uploadProgressListener = uploadProgressListener;
    this.uploaderAnalytics = uploaderAnalytics;
    this.md5Calculator = md5Calculator;
  }

  @Override
  public Single<UploadDraft> startUploadDraft(String md5, String language, String storeName,
      InstalledApp installedApp) {
    return Single.just(new UploadDraft(UploadDraft.Status.START, installedApp, md5));
  }

  @Override public Observable<UploadDraft> createDraft(String md5, InstalledApp installedApp) {
    return accountProvider.getToken()
        .flatMapObservable(
            accessToken -> serviceV7.createDraft(getParamsCreateDraft(accessToken, installedApp))
                .map(response -> mapCreateDraftResponse(response, installedApp, md5))
                .onErrorReturn(
                    throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR, installedApp, md5,
                        0)));
  }

  @Override
  public Observable<UploadDraft> setDraftStatus(UploadDraft draft, DraftStatus draftStatus) {
    return accountProvider.getToken()
        .flatMapObservable(
            accessToken -> serviceV7.setStatus(accessToken, String.valueOf(draft.getDraftId()),
                draftStatus.toString())
                .map(response -> mapSetDraftStatusResponse(response, draft, draftStatus))
                .onErrorReturn(throwable -> {
                  throwable.printStackTrace();
                  return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
                      draft.getMd5(), draft.getDraftId());
                }));
  }

  @Override public Observable<UploadDraft> getDraftStatus(UploadDraft draft) {
    return accountProvider.getToken()
        .flatMapObservable(
            accessToken -> serviceV7.getStatus(accessToken, String.valueOf(draft.getDraftId()))
                .flatMap(response -> {
                  if (response != null && ((response.body()
                      .getData()
                      .getStatus()
                      .equals("PENDING") || response.body()
                      .getData()
                      .getStatus()
                      .equals("PROCESSING")))) {
                    throw new NetworkErrorException();
                  }
                  return Observable.just(mapUploadDraftResponse(response, draft));
                })
                .retryWhen(new RetryWithDelay(5)));
  }

  @Override public Observable<UploadDraft> hasApplicationMetaData(UploadDraft draft) {
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> serviceV7.hasApplicationMetaData(accessToken,
            getParamsMetadataExists(draft.getDraftId()))
            .map(result -> {
              if (result.isSuccessful()) {
                if (result.body() != null && result.body()
                    .getData()
                    .hasMetaData()) {
                  UploadDraft uploadDraft = new UploadDraft(UploadDraft.Status.SET_STATUS_TO_DRAFT,
                      draft.getInstalledApp(), draft.getMd5(), draft.getDraftId());
                  return uploadDraft;
                } else {
                  UploadDraft uploadDraft =
                      new UploadDraft(UploadDraft.Status.NO_META_DATA, draft.getInstalledApp(),
                          draft.getMd5(), draft.getDraftId());
                  return uploadDraft;
                }
              } else {
                UploadDraft uploadDraft =
                    new UploadDraft(UploadDraft.Status.UNKNOWN_ERROR_RETRY, draft.getInstalledApp(),
                        draft.getMd5(), draft.getDraftId());
                return uploadDraft;
              }
            }));
  }

  @Override public Observable<UploadDraft> setDraftMetadata(UploadDraft draft) {
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> serviceV7.setMetadata(accessToken,
            getSetMetadataParams(draft.getDraftId(), draft.getMetadata()))
            .map(response -> mapSetMetadataResponse(response, draft))
            .onErrorReturn(throwable -> {
              throwable.printStackTrace();
              return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
                  draft.getMd5(), draft.getDraftId());
            }));
  }

  @Override public Observable<UploadDraft> uploadFiles(UploadDraft draft) {
    Function<Throwable, UploadDraft> throwableUploadDraftFunction =
        throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
            draft.getMd5(), draft.getDraftId());
    Function<List<Response<GenericDraftResponse>>, UploadDraft> listUploadDraftFunction =
        __ -> new UploadDraft(UploadDraft.Status.WAITING_UPLOAD_CONFIRMATION,
            draft.getInstalledApp(), draft.getMd5(), draft.getDraftId());
    if (Build.VERSION.SDK_INT < 21
        || draft.getInstalledApp()
        .getSplits() == null
        || draft.getInstalledApp()
        .getSplits()
        .isEmpty()) {
      return Observable.fromIterable(draft.getInstalledApp()
          .getRegularApkFiles())
          .concatMap(fileToUpload -> mapTypesToAction(fileToUpload, draft))
          .doOnNext(response -> {
            if (!response.isSuccessful()) {
              throw new NetworkErrorException();
            }
          })
          .toList()
          .toObservable()
          .map(listUploadDraftFunction)
          .onErrorReturn(throwableUploadDraftFunction);
    } else {
      return Observable.fromIterable(draft.getInstalledApp()
          .getAppBundleFiles())
          .concatMap(fileToUpload -> mapTypesToAction(fileToUpload, draft))
          .doOnNext(response -> {
            if (!response.isSuccessful()) {
              throw new NetworkErrorException();
            }
          })
          .toList()
          .toObservable()
          .map(listUploadDraftFunction)
          .onErrorReturn(throwableUploadDraftFunction);
    }
  }

  public Observable<UploadDraft> uploadSplits(UploadDraft draft, List<String> paths) {
    return Observable.fromIterable(paths)
        .concatMap(split -> Observable.just(split)
            .flatMap(__ -> uploadSplit(split, draft.getDraftId(), draft.getInstalledApp()
                .getPackageName()))
            .doOnNext(response -> {
              if (!response.isSuccessful()) {
                throw new NetworkErrorException();
              }
            }))
        .toList()
        .toObservable()
        .map(__ -> new UploadDraft(UploadDraft.Status.WAITING_UPLOAD_CONFIRMATION,
            draft.getInstalledApp(), draft.getMd5(), draft.getDraftId()))
        .onErrorReturn(
            throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
                draft.getMd5(), draft.getDraftId()));
  }

  @Override public Observable<UploadDraft> setDraftMd5s(UploadDraft draft) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return accountProvider.getToken()
          .flatMapObservable(accessToken -> serviceV7.setDraftMd5sAboveLollipop(
              new SetDraftSplitMd5sRequest(accessToken, draft.getDraftId(),
                  getSplitsList(draft.getInstalledApp()), draft.getMd5()))
              .map(response -> mapSetDraftMd5sResponse(response, draft))
              .onErrorReturn(throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR,
                  draft.getInstalledApp(), draft.getMd5(), draft.getDraftId())));
    }
    return accountProvider.getToken()
        .flatMapObservable(accessToken -> serviceV7.setDraftMd5sBelowLollipop(
            getParamsSetDraftMd5s(accessToken, draft.getMd5(), draft.getDraftId()))
            .map(response -> mapSetDraftMd5sResponse(response, draft))
            .onErrorReturn(throwable -> new UploadDraft(UploadDraft.Status.CLIENT_ERROR,
                draft.getInstalledApp(), draft.getMd5(), draft.getDraftId())));
  }

  private Observable<Response<GenericDraftResponse>> mapTypesToAction(
      InstalledApp.FileToUpload fileToUpload, UploadDraft draft) {
    switch (fileToUpload.getType()) {
      case OBB_MAIN:
        return uploadObbMain(draft);
      case OBB_PATCH:
        return uploadObbPatch(draft);
      case SPLIT:
        return uploadSplit(fileToUpload.getPath(), draft.getDraftId(), draft.getInstalledApp()
            .getPackageName());
      case BASE:
      default:
        return uploadBaseApk(draft);
    }
  }

  private Observable<Response<GenericDraftResponse>> uploadBaseApk(UploadDraft draft) {
    return accountProvider.getAccount()
        .firstOrError()
        .flatMapObservable(aptoideAccount -> accountProvider.getToken()
            .toObservable()
            .flatMap(accessToken -> serviceV7.uploadBaseApkFile(
                getParamsSetApkFile(accessToken, draft.getDraftId()),
                MultipartBody.Part.createFormData("apk_file", draft.getInstalledApp()
                    .getApkPath(), createFileRequestBody("apk", draft.getInstalledApp()
                    .getApkPath(), draft.getInstalledApp()
                    .getPackageName())))));
  }

  private Observable<Response<GenericDraftResponse>> uploadObbMain(UploadDraft draft) {
    return accountProvider.getAccount()
        .firstOrError()
        .flatMapObservable(aptoideAccount -> accountProvider.getToken()
            .toObservable()
            .flatMap(accessToken -> serviceV7.uploadObbMainFile(
                getParamsSetApkFile(accessToken, draft.getDraftId()),
                MultipartBody.Part.createFormData("obb_main_file", draft.getInstalledApp()
                    .getObbMainPath(), createFileRequestBody("obb", draft.getInstalledApp()
                    .getObbMainPath(), draft.getInstalledApp()
                    .getPackageName())))));
  }

  private Observable<Response<GenericDraftResponse>> uploadObbPatch(UploadDraft draft) {
    return accountProvider.getAccount()
        .firstOrError()
        .flatMapObservable(aptoideAccount -> accountProvider.getToken()
            .toObservable()
            .flatMap(accessToken -> serviceV7.uploadObbPatchFile(
                getParamsSetApkFile(accessToken, draft.getDraftId()),
                MultipartBody.Part.createFormData("obb_patch_file", draft.getInstalledApp()
                    .getObbPatchPath(), createFileRequestBody("obb", draft.getInstalledApp()
                    .getObbPatchPath(), draft.getInstalledApp()
                    .getPackageName())))));
  }

  private Observable<Response<GenericDraftResponse>> uploadSplit(String splitPath, int draftId,
      String packageName) {
    return accountProvider.getAccount()
        .firstOrError()
        .flatMapObservable(aptoideAccount -> accountProvider.getToken()
            .toObservable()
            .flatMap(
                accessToken -> serviceV7.uploadSplitFile(getParamsSetApkFile(accessToken, draftId),
                    MultipartBody.Part.createFormData("file", splitPath,
                        createFileRequestBody("apk", splitPath, packageName)))));
  }

  @NonNull private Map<String, okhttp3.RequestBody> getParamsCreateDraft(String token,
      InstalledApp installedApp) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), token));
    parameters.put("package_name",
        RequestBody.create(MediaType.parse("text/plain"), installedApp.getPackageName()));
    parameters.put("vercode", RequestBody.create(MediaType.parse("text/plain"),
        String.valueOf(installedApp.getVersionCode())));
    return parameters;
  }

  @NonNull
  private Map<String, RequestBody> getParamsSetDraftMd5s(String token, String md5, int draftId) {
    Map<String, RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), token));
    parameters.put("draft_id",
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(draftId)));
    parameters.put("apk_md5sum", RequestBody.create(MediaType.parse("text/plain"), md5));
    return parameters;
  }

  private List<String> getSplitsList(InstalledApp installedApp) {
    ArrayList<String> list = new ArrayList<>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (!installedApp.getSplits()
          .isEmpty()) {
        for (InstalledApp.FileToUpload split : installedApp.getSplits()) {
          if (split.getPath()
              .contains("split_config")) {
            list.add(md5Calculator.calculate(split.getPath())
                .blockingGet());
          }
        }
      }
    }
    return list;
  }

  @NonNull private Map<String, okhttp3.RequestBody> getParamsMetadataExists(int draftId) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("draft_id",
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(draftId)));
    Log.w(getClass().getSimpleName(), parameters.toString());
    return parameters;
  }

  @NonNull
  private Map<String, okhttp3.RequestBody> getParamsSetApkFile(String accessToken, int draftId) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("access_token", RequestBody.create(MediaType.parse("text/plain"), accessToken));
    parameters.put("draft_id",
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(draftId)));
    Log.w(getClass().getSimpleName(), parameters.toString());
    return parameters;
  }

  private Map<String, RequestBody> getSetMetadataParams(int draftId, Metadata metadata) {
    Map<String, okhttp3.RequestBody> parameters = new HashMap<>();
    parameters.put("draft_id",
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(draftId)));
    parameters.put("apk_name",
        RequestBody.create(MediaType.parse("text/plain"), metadata.getName()));
    parameters.put("description",
        RequestBody.create(MediaType.parse("text/plain"), metadata.getDescription()));
    parameters.put("category",
        RequestBody.create(MediaType.parse("text/plain"), metadata.getCategory()));
    parameters.put("age_rating",
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

  @NonNull private UploadDraft mapCreateDraftResponse(Response<GenericDraftResponse> response,
      InstalledApp installedApp, String md5) {
    if (response.body()
        .getInfo()
        .getStatus()
        .equals(Status.FAIL)) {
      sendAnalytics("fail", response);
      return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, installedApp, md5);
    }
    return new UploadDraft(UploadDraft.Status.DRAFT_CREATED, installedApp, md5,
        response.body().data.getDraftId());
  }

  @NonNull private UploadDraft mapUploadDraftResponse(Response<GenericDraftResponse> response,
      UploadDraft draft) {
    if (response.body()
        .getData()
        .getStatus()
        .equals("ERROR")) {
      switch (response.body()
          .getData()
          .getError()
          .get(0)
          .getCode()) {
        case "APK-103":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.DUPLICATE, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "APK-101":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.INTELLECTUAL_RIGHTS, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "APK-102":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.INFECTED, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "APK-106":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.INVALID_SIGNATURE, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "APK-104":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.PUBLISHER_ONLY, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "APK-5":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.NOT_EXISTENT, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "SPLIT-1":
          List<String> splitsMissing = response.body()
              .getData()
              .getError()
              .get(0)
              .getDetails()
              .getSplits();
          UploadDraft uploadDraft =
              new UploadDraft(UploadDraft.Status.UPLOAD_PENDING, draft.getInstalledApp(),
                  draft.getMd5(), draft.getDraftId());
          uploadDraft.setSplitsToBeUploaded(splitsMissing);
          return uploadDraft;
        case "SYS-1":
        case "REPO-9":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.UNKNOWN_ERROR_RETRY, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "FILE-5":
        case "FILE-200":
        case "FILE-202":
        case "FILE-206":
        case "APK-109":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.UPLOAD_FAILED_RETRY, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "MARG-5":
        case "MARG-101":
        case "MARG-102":
        case "MARG-205":
        case "QUOTA-1":
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.UPLOAD_FAILED, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
        case "SPLIT-2": //AAB not supported yet.
        case "APK-107":
        case "FILE-111":
        case "FILE-112":
        case "IARG-1":
        default:
          sendAnalytics("fail", response);
          return new UploadDraft(UploadDraft.Status.UNKNOWN_ERROR, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
      }
    }
    uploaderAnalytics.sendUploadCompleteEvent("success", "Upload App to Repo", "0", "0");
    return new UploadDraft(UploadDraft.Status.COMPLETED, draft.getInstalledApp(), draft.getMd5(),
        draft.getDraftId());
  }

  @NonNull private UploadDraft mapSetDraftStatusResponse(Response<GenericDraftResponse> response,
      UploadDraft draft, DraftStatus draftStatus) {
    if (response.body()
        .getInfo()
        .getStatus()
        .equals(Status.FAIL)) {
      sendAnalytics("fail", response);
      return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
          draft.getMd5(), draft.getDraftId());
    }
    if (draftStatus.equals(DraftStatus.DRAFT)) {
      UploadDraft newDraft =
          new UploadDraft(UploadDraft.Status.STATUS_SET_DRAFT, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
      if (draft.getSplitsToBeUploaded() != null) {
        newDraft.setSplitsToBeUploaded(draft.getSplitsToBeUploaded());
      }
      return newDraft;
    } else {
      UploadDraft newDraft =
          new UploadDraft(UploadDraft.Status.STATUS_SET_PENDING, draft.getInstalledApp(),
              draft.getMd5(), draft.getDraftId());
      if (draft.getSplitsToBeUploaded() != null) {
        newDraft.setSplitsToBeUploaded(draft.getSplitsToBeUploaded());
      }
      return newDraft;
    }
  }

  @NonNull private UploadDraft mapSetDraftMd5sResponse(Response<GenericDraftResponse> response,
      UploadDraft draft) {
    if (response.body()
        .getInfo()
        .getStatus()
        .equals(Status.FAIL)) {
      sendAnalytics("fail", response);
      return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
          draft.getMd5(), draft.getDraftId());
    }
    return new UploadDraft(UploadDraft.Status.MD5S_SET, draft.getInstalledApp(), draft.getMd5(),
        draft.getDraftId());
  }

  @NonNull private UploadDraft mapSetMetadataResponse(Response<GenericDraftResponse> response,
      UploadDraft draft) {
    if (response.body()
        .getInfo()
        .getStatus()
        .equals(Status.FAIL)) {
      sendAnalytics("fail", response);
      return new UploadDraft(UploadDraft.Status.CLIENT_ERROR, draft.getInstalledApp(),
          draft.getMd5(), draft.getDraftId());
    }
    return new UploadDraft(UploadDraft.Status.METADATA_SET, draft.getInstalledApp(), draft.getMd5(),
        draft.getDraftId());
  }

  private void sendAnalytics(String status, Response<GenericDraftResponse> response) {

    /*
    uploaderAnalytics.sendUploadCompleteEvent(status, "Upload App to Repo", response.body()
        .getData()
        .getError()
        .get(0)
        .getCode(), response.body()
        .getError()
        .getDescription());*/
  }

  public interface ServiceV7 {
    @Multipart @POST("7/uploader/draft/create")
    Observable<Response<GenericDraftResponse>> createDraft(
        @PartMap Map<String, okhttp3.RequestBody> params);

    @GET("7/uploader/draft/status/set") Observable<Response<GenericDraftResponse>> setStatus(
        @Query("access_token") String accessToken, @Query("draft_id") String draftId,
        @Query("status") String status);

    @GET("7/uploader/draft/status/get") Observable<Response<GenericDraftResponse>> getStatus(
        @Query("access_token") String accessToken, @Query("draft_id") String draftId);

    @Multipart @POST("7/uploader/draft/apk/set")
    Observable<Response<GenericDraftResponse>> uploadBaseApkFile(
        @PartMap Map<String, okhttp3.RequestBody> params, @Part MultipartBody.Part apkFile);

    @Multipart @POST("7/uploader/draft/apk/split/set")
    Observable<Response<GenericDraftResponse>> uploadSplitFile(
        @PartMap Map<String, okhttp3.RequestBody> params, @Part MultipartBody.Part splitFile);

    @Multipart @POST("7/uploader/draft/apk/set")
    Observable<Response<GenericDraftResponse>> setDraftMd5sBelowLollipop(
        @PartMap Map<String, okhttp3.RequestBody> params);

    @POST("7/uploader/draft/apk/set")
    Observable<Response<GenericDraftResponse>> setDraftMd5sAboveLollipop(
        @Body SetDraftSplitMd5sRequest request);

    @Multipart @POST("7/uploader/draft/obb/set")
    Observable<Response<GenericDraftResponse>> uploadObbMainFile(
        @PartMap Map<String, okhttp3.RequestBody> params, @Part MultipartBody.Part obbMain);

    @Multipart @POST("7/uploader/draft/obb/set")
    Observable<Response<GenericDraftResponse>> uploadObbPatchFile(
        @PartMap Map<String, okhttp3.RequestBody> params, @Part MultipartBody.Part obbPatch);

    @Multipart @POST("7/uploader/draft/metadata/exists")
    Observable<Response<HasApplicationMetaDataResponse>> hasApplicationMetaData(
        @Query("access_token") String accessToken,
        @PartMap Map<String, okhttp3.RequestBody> params);

    @Multipart @POST("7/uploader/draft/metadata/set")
    Observable<Response<GenericDraftResponse>> setMetadata(
        @Query("access_token") String accessToken,
        @PartMap Map<String, okhttp3.RequestBody> params);
  }
}
