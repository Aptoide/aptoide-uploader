/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pt.caixamagica.aptoide.uploader.retrofit.LoginErrorException;
import pt.caixamagica.aptoide.uploader.webservices.json.OAuth;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by neuro on 29-01-2015.
 */
@ToString public class OAuth2AuthenticationRequest
    extends RetrofitSpiceRequest<OAuth, OAuth2AuthenticationRequest.Webservice> {

  public final Bean bean = new Bean();

  public OAuth2AuthenticationRequest() {
    super(OAuth.class, Webservice.class);
  }

  @Override public OAuth loadDataFromNetwork() throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    String s = objectMapper.writeValueAsString(bean);

    HashMap hashMap = objectMapper.convertValue(bean, HashMap.class);
    s = objectMapper.writeValueAsString(hashMap);

    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put("grant_type", bean.getGrant_type());
    parameters.put("client_id", "Aptoide");
    parameters.put("mode", "json");

    if (bean.getAuthMode() != null) {
      switch (bean.getAuthMode()) {
        case aptoide:
          parameters.put("username", bean.getUsername());
          parameters.put("password", bean.getPassword());
          break;
        case google:
          parameters.put("authMode", "google");
          parameters.put("oauthUserName", bean.getNameForGoogle());
          parameters.put("oauthToken", bean.getPassword());
          break;
        case facebook_uploader:
          parameters.put("authMode", "facebook_uploader");
          parameters.put("oauthToken", bean.getPassword());
          parameters.put("password", bean.getPassword());
          break;
      }
    } else {
      parameters.put("username", bean.getUsername());
      parameters.put("password", bean.getPassword());
    }

    if (bean.getRepo() != null) {
      if (bean.getAuthMode() == Mode.facebook_uploader || bean.getAuthMode() == Mode.google) {
        parameters.put("oauthCreateRepo", "1");
      } else {
        parameters.put("createRepo", bean.getCreateRepo());
      }
      parameters.put("repo", bean.getRepo());
      if (bean.getPrivacy_user() != null && bean.getPrivacy_pass() != null) {
        parameters.put("privacy_user", bean.getPrivacy_user());
        parameters.put("privacy_pass", bean.getPrivacy_pass());
      }
    }

    OAuth response = null;

    try {
      response = getService().oauth2Authentication(hashMap);
    } catch (RetrofitError error) {
      error.printStackTrace();

      throw new LoginErrorException();
    }

    return response;
  }

  public enum Mode {
    aptoide, google, facebook_uploader;

    @Override public String toString() {
      return super.toString()
          .toLowerCase();
    }
  }

  public interface Webservice {

    @POST("/3/oauth2Authentication") @FormUrlEncoded OAuth oauth2Authentication(
        @FieldMap HashMap<String, String> args);
  }

  public class Bean {

    @JsonProperty("mode") public final String tmp = "json";
    public final String client_id = "Aptoide";
    @Getter @Setter private String grant_type = "password";
    @Getter @Setter private String username;
    @Getter @Setter private String password;
    @Getter @Setter private String oauthToken;
    @Getter @Setter private String oauthUserName;
    @Getter @Setter private String oauthCreateRepo;
    @Getter @Setter private String createRepo;
    @Getter @Setter private String isPrivate;
    @Getter @Setter private String repo;
    // Private store
    @Getter @Setter private String privacy_user;
    @Getter @Setter private String privacy_pass;
    @Getter @Setter private Mode authMode;
    @Getter @Setter private String nameForGoogle;
    @Getter @Setter private String refresh_token;
  }
}
