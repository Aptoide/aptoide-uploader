package com.aptoide.uploader;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by jdandrade on 03/11/2017.
 */

public class RetrofitAccountService implements AccountService {

  private final Service service;
  private final AccountResponseMapper mapper;

  public RetrofitAccountService(Service service, AccountResponseMapper mapper) {
    this.service = service;
    this.mapper = mapper;
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
        .flatMap(oAuth -> {
          if (oAuth.hasErrors()){
            return Single.error(new IllegalStateException(oAuth.getError()));
          }
          return service.getUserInfo(new AccountRequestBody(Arrays.asList("meta"), oAuth.getAccessToken())).singleOrError();
        })
            .flatMap(response -> {
              if(response.isOk()){
                return Single.just(mapper.map(response));
              }
              return Single.error(new IllegalStateException(response.getError().getDescription()));
            });
  }

  public interface Service {
    @POST("oauth2Authentication") @FormUrlEncoded Observable<OAuth> oauth2Authentication(
        @FieldMap Map<String, String> args);

    @POST("user/get") Observable<AccountResponse> getUserInfo(@Body AccountRequestBody body);
  }
}
