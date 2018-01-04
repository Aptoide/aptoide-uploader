package com.aptoide.uploader.security;

import com.aptoide.uploader.account.network.OAuth;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by jdandrade on 03/01/2018.
 */

public class AptoideAccessTokenProvider implements AuthenticationProvider {
  private static final String RESPONSE_MODE = "json";
  private static final String ACCOUNT_GRANT_TYPE = "password";
  private static final String ACCOUNT_CLIENT_ID = "Aptoide";
  private final ServiceV3 serviceV3;
  private String accessToken;
  private String refreshToken;

  public AptoideAccessTokenProvider(ServiceV3 serviceV3) {
    this.serviceV3 = serviceV3;
  }

  @Override public void saveAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override public void saveRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Override public String getRefreshToken() {
    return this.refreshToken;
  }

  @Override public Single<String> getAccessToken(String username, String password) {
    if (accessToken == null) {
      final Map<String, String> args = new HashMap<>();
      args.put("username", username);
      args.put("password", password);
      args.put("grant_type", ACCOUNT_GRANT_TYPE);
      args.put("client_id", ACCOUNT_CLIENT_ID);
      args.put("mode", RESPONSE_MODE);
      return serviceV3.oauth2Authentication(args)
          .singleOrError()
          .flatMap(response -> {
            OAuth body = response.body();
            if (response.isSuccessful() && body != null && !body.hasErrors()) {
              accessToken = body.getAccessToken();
              refreshToken = body.getRefreshToken();
              return Single.just(body.getAccessToken());
            }
            return Single.error(new IllegalStateException(response.message()));
          });
    }
    return Single.just(refreshToken);
  }

  public interface ServiceV3 {
    @POST("webservices/3/oauth2Authentication") @FormUrlEncoded
    Observable<Response<OAuth>> oauth2Authentication(@FieldMap Map<String, String> args);
  }
}
