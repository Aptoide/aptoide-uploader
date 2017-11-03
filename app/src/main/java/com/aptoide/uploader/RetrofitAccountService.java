package com.aptoide.uploader;

import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.Map;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by jdandrade on 03/11/2017.
 */

public class RetrofitAccountService implements AccountService {

  private final Service service;

  public RetrofitAccountService(Service service) {
    this.service = service;
  }

  @Override public Single<AptoideAccount> getAccount(String username, String password) {
    final Map<String, String> args = new HashMap<>();
    args.put("username", username);
    args.put("password", password);
    args.put("grant_type", "password");
    args.put("client_id", "Aptoide");
    args.put("mode", "json");
    return service.oauth2Authentication(args)
        .singleOrError()
        .map(oAuth -> new AptoideAccount());
  }

  public interface Service {
    @POST("oauth2Authentication") @FormUrlEncoded Observable<OAuth> oauth2Authentication(
        @FieldMap Map<String, String> args);
  }
}
