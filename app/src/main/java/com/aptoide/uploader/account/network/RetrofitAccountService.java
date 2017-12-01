package com.aptoide.uploader.account.network;

import com.aptoide.uploader.account.AccountService;
import com.aptoide.uploader.account.AptoideAccount;
import com.aptoide.uploader.security.SecurityAlgorithms;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

  public static final String RESPONSE_MODE = "json";
  public static final String ACCOUNT_GRANT_TYPE = "password";
  public static final String ACCOUNT_CLIENT_ID = "Aptoide";
  private final ServiceV2 serviceV2;
  private final ServiceV3 serviceV3;
  private final ServiceV7 serviceV7;
  private final SecurityAlgorithms securityAlgorithms;
  private final AccountResponseMapper mapper;

  public RetrofitAccountService(ServiceV2 serviceV2, ServiceV3 serviceV3, ServiceV7 serviceV7,
      SecurityAlgorithms securityAlgorithms, AccountResponseMapper mapper) {
    this.serviceV2 = serviceV2;
    this.serviceV3 = serviceV3;
    this.serviceV7 = serviceV7;
    this.securityAlgorithms = securityAlgorithms;
    this.mapper = mapper;
  }

  @Override public Single<AptoideAccount> getAccount(String username, String password) {
    final Map<String, String> args = new HashMap<>();
    args.put("username", username);
    args.put("password", password);
    args.put("grant_type", ACCOUNT_GRANT_TYPE);
    args.put("client_id", ACCOUNT_CLIENT_ID);
    args.put("mode", RESPONSE_MODE);

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

  @Override
  public Single<AptoideAccount> createAccount(String email, String password, String storeName) {
    return createAccount(email, password, storeName, null, null);
  }

  @Override public Single<AptoideAccount> createAccount(String userEmail, String userPassword,
      String storeName, String storeUser, String storePassword) {

    String passwordHash;
    try {
      passwordHash = securityAlgorithms.calculateHash(userPassword);
    } catch (Exception e) {
      return Single.error(e);
    }

    final boolean isPrivateStore = storePassword != null && !storePassword.isEmpty();

    Map<String, String> parameters = new HashMap<>();
    parameters.put("mode", RESPONSE_MODE);
    parameters.put("email", userEmail);
    parameters.put("passwordHash", passwordHash);
    parameters.put("repo", storeName);
    parameters.put("privacy", Boolean.toString(isPrivateStore));
    if (isPrivateStore) {
      parameters.put("privacy_user", storeUser);
      parameters.put("privacy_pass", storePassword);
    }

    List<String> fields = new ArrayList<>();
    fields.add(userEmail);
    fields.add(passwordHash);
    fields.add(storeName);
    fields.add(Boolean.toString(isPrivateStore));
    if (isPrivateStore) {
      fields.add(storeUser);
      fields.add(storePassword);
    }

    String hash;
    try {
      hash = securityAlgorithms.calculateIntegrityWithKey(fields, "bazaar_hmac");
    } catch (Exception e) {
      return Single.error(e);
    }
    parameters.put("hmac", hash);

    return serviceV2.createAccount(parameters)
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

  public interface ServiceV2 {
    @POST("webservices/2/createUser") @FormUrlEncoded Observable<Response<OAuth>> createAccount(
        @FieldMap Map<String, String> args);
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
