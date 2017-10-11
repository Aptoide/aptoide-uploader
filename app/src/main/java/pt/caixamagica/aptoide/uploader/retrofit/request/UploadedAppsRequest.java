/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import lombok.Data;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;
import retrofit.http.POST;

/**
 * Created by neuro on 30-01-2015.
 */
public class UploadedAppsRequest
    extends RetrofitSpiceRequest<UserCredentialsJson, UploadedAppsRequest.Webservice> {

  private Body body;

  public UploadedAppsRequest(String access_token, String store_name, String apk_md5sums) {
    super(UserCredentialsJson.class, UploadedAppsRequest.Webservice.class);
    this.body = new Body(access_token, store_name, apk_md5sums);
  }

  @Override public UserCredentialsJson loadDataFromNetwork() throws Exception {
    return getService().getUploadedApps(body);
  }

  public interface Webservice {
    @POST("/my/apps/apks/get/") UserCredentialsJson getUploadedApps(@retrofit.http.Body Body args);
  }

  @Data private class Body {

    private final String access_token;
    private final String store_name;
    private final String apk_md5sums;

    private Body(String access_token, String store_name, String apk_md5sums) {
      this.access_token = access_token;
      this.store_name = store_name;
      this.apk_md5sums = apk_md5sums;
    }
  }
}
