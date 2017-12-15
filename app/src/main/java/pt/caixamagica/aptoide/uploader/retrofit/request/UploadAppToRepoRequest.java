/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import pt.caixamagica.aptoide.uploader.UploaderUtils;
import pt.caixamagica.aptoide.uploader.retrofit.OAuth2Request;
import pt.caixamagica.aptoide.uploader.uploadService.RequestProgressListener;
import pt.caixamagica.aptoide.uploader.webservices.json.UploadAppToRepoJson;
import retrofit.RetrofitError;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PartMap;
import retrofit.mime.TypedFile;

/**
 * Created by neuro on 22-02-2015.
 */
@Data @EqualsAndHashCode(callSuper = false) public class UploadAppToRepoRequest
    extends RetrofitSpiceRequest<UploadAppToRepoJson, UploadAppToRepoRequest.Webservice> {

  private static final String TAG = UploadAppToRepoRequest.class.getSimpleName();

  // Flags de controlo de existencia no server.
  @Setter private boolean FLAG_APK = false;

  @Setter private boolean FLAG_MAIN_OBB = false;

  @Setter private boolean FLAG_PATCH_OBB = false;

  private RequestProgressListener requestProgressListenerObject;

  private String token;

  private String repo;

  private String apkName;

  private String apkPath;

  private String packageName;

  private String description;

  private Integer category;

  private Integer rating;

  private String apkPhone;

  private String apkEmail;

  private String apkWebsite;

  private List<String> screenshotsUri;

  private boolean onlyUserRepo = false;

  private String apkMd5sum;

  private int uploadType = 2;

  private String hmac;

  private String uploadFrom;

  private String obbMainPath;

  private String obbPatchPath;

  private String obbMainMd5sum;

  private String obbPatchMd5sum;

  private String inputTitle = null;

  /**
   * Label cachada por questões de performance.
   */
  private String label;
  private String lang;

  private boolean checked = false;
  private StoreTokenInterface storeTokenInterface;

  public UploadAppToRepoRequest(UploadAppToRepoRequest uploadAppToRepoRequest,
      StoreTokenInterface storeTokenInterface) {
    this(uploadAppToRepoRequest.getRequestProgressListenerObject(), storeTokenInterface);

    this.storeTokenInterface = storeTokenInterface;

    token = uploadAppToRepoRequest.token;
    repo = uploadAppToRepoRequest.repo;
    packageName = uploadAppToRepoRequest.packageName;
    apkPath = uploadAppToRepoRequest.apkPath;
    uploadType = uploadAppToRepoRequest.uploadType;
    hmac = uploadAppToRepoRequest.hmac;

    label = uploadAppToRepoRequest.label;
  }

  public UploadAppToRepoRequest(RequestProgressListener requestProgressListenerObject,
      StoreTokenInterface storeTokenInterface) {
    this(storeTokenInterface);
    this.requestProgressListenerObject = requestProgressListenerObject;
  }

  public UploadAppToRepoRequest(StoreTokenInterface storeTokenInterface) {
    super(UploadAppToRepoJson.class, UploadAppToRepoRequest.Webservice.class);
    this.storeTokenInterface = storeTokenInterface;
  }

  @Override public UploadAppToRepoJson loadDataFromNetwork() throws SpiceException {

    final HashMap<String, Object> parameters = new HashMap<>();

    try {
      setRequestProgressListener(requestProgressListenerObject);

      checkObbExistence();

      parameters.put("access_token", token);
      parameters.put("repo", repo);
      parameters.put("apkname", apkName);
      parameters.put("description", description);
      parameters.put("category", category);
      parameters.put("rating", rating);
      parameters.put("apk_phone", apkPhone);
      parameters.put("apk_email", apkEmail);
      parameters.put("apk_website", apkWebsite);
      parameters.put("only_user_repo", onlyUserRepo);
      parameters.put("uploadType", uploadType);
      parameters.put("hmac", hmac);
      parameters.put("upload_from", uploadFrom);
      parameters.put("obb_main_filename", fileName(obbMainPath));
      parameters.put("obb_patch_filename", fileName(obbPatchPath));
      parameters.put("mode", "json");
      parameters.put("inputTitle", inputTitle);
      parameters.put("lang", lang);

      if (FLAG_APK) {
        parameters.put("apk", newTweakedTypedFile("apk", apkPath));
      } else {
        //Used in first request to check if apk already exists
        parameters.put("apk_md5sum", UploaderUtils.md5Calc(new File(apkPath)));
      }

      if (obbMainPath != null) {
        if (FLAG_MAIN_OBB) {
          parameters.put("obb_main", newTweakedTypedFile("obb", obbMainPath));
        } else {
          parameters.put("obb_main_md5sum", UploaderUtils.md5Calc(new File(obbMainPath)));
        }
      }

      if (obbPatchPath != null) {
        if (FLAG_PATCH_OBB) {
          parameters.put("obb_patch", newTweakedTypedFile("obb", obbPatchPath));
        } else {
          parameters.put("obb_patch_md5sum", UploaderUtils.md5Calc(new File(obbPatchPath)));
        }
      }

      UploadAppToRepoJson response = getService().uploadAppToRepo(parameters);

      return response;
    } catch (RetrofitError e) {
      Log.e(TAG, "RetrofitError: ", e);

      // Trick para forçar a chamada do onRequestFailure pois aparentemente nem sempre isso acontece
      if (e != null && e.getBody() != null && (("The access token provided is invalid").equals(
          ((UploadAppToRepoJson) e.getBody()).getError_description())
          || ("The access token provided has expired").equals(
          ((UploadAppToRepoJson) e.getBody()).getError_description()))) {

        OAuth2Request oAuth2Request = new OAuth2Request();
        token = oAuth2Request.builder();
        parameters.put("access_token", token);

        storeTokenInterface.setToken(token);

        return getService().uploadAppToRepo(parameters);
      } else {
        throw new SpiceException(e);
      }
    }
  }

  @Override protected void publishProgress(float progress) {
    super.publishProgress(progress);
  }

  private void checkObbExistence() {
    if (!checked) {
      String sdcard = Environment.getExternalStorageDirectory()
          .getAbsolutePath();
      File obbDir = new File(sdcard + "/Android/obb/" + getPackageName() + "/");
      if (obbDir.isDirectory()) {

        File[] files = obbDir.listFiles();

        if (files != null) {
          for (File file : files) {

            if (file.getName()
                .contains("main") && !file.getName()
                .contains("--downloading")) {
              obbMainPath = file.getAbsolutePath();
            } else if (file.getName()
                .contains("patch") && !file.getName()
                .contains("--downloading")) {
              obbPatchPath = file.getAbsolutePath();
            }
          }
        }
      }
      checked = true;
    }
  }

  private String fileName(String apkPath) {

    if (apkPath == null) return null;

    return apkPath.substring(apkPath.lastIndexOf("/") + 1);
  }

  private TypedFile newTweakedTypedFile(String extension, String apkPath) {

    String mimeType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension);

    if (mimeType == null) mimeType = "application/octet-stream";

    final File file = new File(apkPath);
    return new TypedFile(mimeType, file) {

      @Override public void writeTo(OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        FileInputStream in = new FileInputStream(file);

        long fileSize = file.length();

        long percentageTicks = fileSize / 4096 / 100;

        int parts = 0;
        float progress = 0;

        try {
          int read;
          while ((read = in.read(buffer)) != -1 && !isCancelled()) {
            out.write(buffer, 0, read);
            parts++;
            if (percentageTicks > 0 && parts % percentageTicks == 0) {
              progress = (float) parts * buffer.length / fileSize * 100;
              publishProgress(progress);
            }
          }
          if (read == -1) {
            publishProgress(100);
          }
        } finally {
          in.close();
        }
      }
    };
  }

  public interface Webservice {

    @Multipart @POST("/3/uploadAppToRepo") UploadAppToRepoJson uploadAppToRepo(
        @PartMap Map<String, Object> params);
  }
}
