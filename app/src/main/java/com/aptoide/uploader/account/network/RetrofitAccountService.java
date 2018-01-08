package com.aptoide.uploader.account.network;

import com.aptoide.uploader.account.AccountService;
import com.aptoide.uploader.account.AptoideAccount;
import com.aptoide.uploader.account.network.error.DuplicatedStoreException;
import com.aptoide.uploader.account.network.error.DuplicatedUserException;
import com.aptoide.uploader.security.AuthenticationProvider;
import com.aptoide.uploader.security.SecurityAlgorithms;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.Collections;
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
  private final ServiceV3 serviceV3;
  private final ServiceV7 serviceV7;
  private final SecurityAlgorithms securityAlgorithms;
  private final AccountResponseMapper mapper;
  private final AuthenticationProvider authenticationProvider;

  public RetrofitAccountService(ServiceV3 serviceV3, ServiceV7 serviceV7,
      SecurityAlgorithms securityAlgorithms, AccountResponseMapper mapper,
      AuthenticationProvider authenticationProvider) {
    this.serviceV3 = serviceV3;
    this.serviceV7 = serviceV7;
    this.securityAlgorithms = securityAlgorithms;
    this.mapper = mapper;
    this.authenticationProvider = authenticationProvider;
  }

  @Override public Single<AptoideAccount> getAccount(String username, String password) {
    return authenticationProvider.getAccessToken(username, password)
        .flatMap(accessToken -> serviceV7.getUserInfo(
            new AccountRequestBody(Collections.singletonList("meta"), accessToken))
            .singleOrError())
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
    parameters.put("passhash", passwordHash);
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

    return serviceV3.createAccount(parameters)
        .singleOrError()
        .flatMap(response -> {
          final OAuth body = response.body();

          if (body != null && body.hasErrors()) {
            if (DuplicatedStoreException.CODE.equalsIgnoreCase(body.getError())) {
              return Single.error(new DuplicatedStoreException());
            }

            if (DuplicatedUserException.CODE.equalsIgnoreCase(body.getError())) {
              return Single.error(new DuplicatedUserException());
            }
          }

          if (response.isSuccessful() && body != null && !body.hasErrors()) {
            authenticationProvider.saveAuthentication(body.getAccessToken(),
                body.getRefreshToken());
            return serviceV7.getUserInfo(
                new AccountRequestBody(Collections.singletonList("meta"), body.getAccessToken()))
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
    @POST("webservices/3/createUser") @FormUrlEncoded Observable<Response<OAuth>> createAccount(
        @FieldMap Map<String, String> args);
  }

  public interface ServiceV7 {
    @POST("api/7/user/get") Observable<Response<AccountResponse>> getUserInfo(
        @Body AccountRequestBody body);
  }
}
