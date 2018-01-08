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
  private final AuthenticationPersistance authenticationPersistance;

  public AptoideAccessTokenProvider(AuthenticationPersistance authenticationPersistance,
      ServiceV3 serviceV3) {
    this.authenticationPersistance = authenticationPersistance;
    this.serviceV3 = serviceV3;
  }

  @Override public Single<String> getAccessToken(String username, String password) {
    String accessToken = authenticationPersistance.getAccessToken();
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
              authenticationPersistance.saveAuthentication(body.getAccessToken(),
                  body.getRefreshToken());
              return Single.just(body.getAccessToken());
            }
            return Single.error(new IllegalStateException(response.message()));
          });
    }
    return Single.just(accessToken);
  }

  @Override public Single<String> getAccessToken() {
    String accessToken = authenticationPersistance.getAccessToken();
    if (accessToken == null) {
      return Single.error(new IllegalStateException("There is no access token!"));
    }
    return Single.just(accessToken);
  }

  @Override public Single<String> getRefreshToken() {
    String refreshToken = authenticationPersistance.getRefreshToken();
    if (refreshToken == null) {
      return Single.error(new IllegalStateException("There is no refresh token!"));
    }
    return Single.just(refreshToken);
  }

  @Override public void saveAuthentication(String accessToken, String refreshToken) {
    authenticationPersistance.saveAuthentication(accessToken, refreshToken);
  }

  public interface ServiceV3 {
    @POST("webservices/3/oauth2Authentication") @FormUrlEncoded
    Observable<Response<OAuth>> oauth2Authentication(@FieldMap Map<String, String> args);
  }
}
