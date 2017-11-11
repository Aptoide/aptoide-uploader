package com.aptoide.uploader.account.network;

import com.aptoide.uploader.account.AptoideAccount;
import com.aptoide.uploader.account.AccountService;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by jdandrade on 03/11/2017.
 */

public class RetrofitAccountService implements AccountService {

  private final ServiceV3 serviceV3;
  private final ServiceV7 serviceV7;
  private final AccountResponseMapper mapper;

  public RetrofitAccountService(ServiceV3 serviceV3, ServiceV7 serviceV7,
      AccountResponseMapper mapper) {
    this.serviceV3 = serviceV3;
    this.serviceV7 = serviceV7;
    this.mapper = mapper;
  }

  @Override public Single<AptoideAccount> getAccount(String username, String password) {
    final Map<String, String> args = new HashMap<>();
    args.put("username", username);
    args.put("password", password);
    args.put("grant_type", "password");
    args.put("client_id", "Aptoide");
    args.put("mode", "json");
    return serviceV3.oauth2Authentication(args)
        .singleOrError()
        .flatMap(response -> {
          if (response.isSuccessful() && !response.body()
              .hasErrors()) {
            return serviceV7.getUserInfo(new AccountRequestBody(Arrays.asList("meta"),
                response.body()
                    .getAccessToken()))
                .singleOrError();
          }

          return Single.error(new IllegalStateException(response.message()));
        })
            .flatMap(response -> {
              if (response.isSuccessful() && response.body()
                  .isOk()) {
                return Single.just(mapper.map(response.body()));
              }
              return Single.error(new IllegalStateException(response.message()));
            });
  }

  public interface ServiceV3 {
    @POST("webservices/3/oauth2Authentication") @FormUrlEncoded
    Observable<Response<OAuth>> oauth2Authentication(@FieldMap Map<String, String> args);
  }

  public interface ServiceV7 {
    @POST("api/7/user/get") Observable<Response<AccountResponse>> getUserInfo(
        @Body AccountRequestBody body);
  }
}
