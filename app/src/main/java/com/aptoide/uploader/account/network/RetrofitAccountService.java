package com.aptoide.uploader.account.network;

import com.aptoide.authentication.model.CodeAuth;
import com.aptoide.authenticationrx.AptoideAuthenticationRx;
import com.aptoide.uploader.account.Account;
import com.aptoide.uploader.account.AccountFactory;
import com.aptoide.uploader.account.AccountService;
import com.aptoide.uploader.account.AutoLoginCredentials;
import com.aptoide.uploader.account.BaseAccount;
import com.aptoide.uploader.account.network.error.DuplicatedStoreException;
import com.aptoide.uploader.security.AuthenticationProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.io.IOException;
import java.util.Collections;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by jdandrade on 03/11/2017.
 */

public class RetrofitAccountService implements AccountService {

  private final ServiceV3 serviceV3;
  private final ServiceV7 serviceV7;
  private final AccountResponseMapper mapper;
  private final AuthenticationProvider authenticationProvider;
  private final AptoideAuthenticationRx aptoideAuthentication;

  public RetrofitAccountService(ServiceV3 serviceV3, ServiceV7 serviceV7,
      AccountResponseMapper mapper, AuthenticationProvider authenticationProvider,
      AptoideAuthenticationRx aptoideAuthentication) {
    this.serviceV3 = serviceV3;
    this.serviceV7 = serviceV7;
    this.mapper = mapper;
    this.authenticationProvider = authenticationProvider;
    this.aptoideAuthentication = aptoideAuthentication;
  }

  @Override public Single<Account> getAccount(String email, String token, String authMode) {
    return authenticationProvider.getAccessTokenOAuth(email, token, authMode)
        .flatMap(accessToken -> serviceV7.getUserInfo(
            new AccountRequestBody(Collections.singletonList("meta"), accessToken))
            .singleOrError())
        .flatMap(response -> {
          if (response.isSuccessful() && response.body()
              .isOk()) {
            BaseAccount.LoginType loginType;
            if (authMode.equals("facebook_uploader")) {
              return Single.just(mapper.map(response.body(), BaseAccount.LoginType.FACEBOOK));
            } else if (authMode.equals("google")) {
              return Single.just(mapper.map(response.body(), BaseAccount.LoginType.GOOGLE));
            } else {
              return Single.error(new IllegalStateException(response.message()));
            }
          }
          return Single.error(new IllegalStateException(response.message()));
        });
  }

  @Override public Single<CreateStoreStatus> createStore(String storeName, String privateUserName,
      String privatePassword, boolean privacyFlag) {
    return authenticationProvider.getAccessToken()
        .flatMap(accessToken -> serviceV3.createRepo(storeName, privateUserName, privatePassword,
            privacyFlag, 1, true, accessToken, "aptoide", accessToken, "json")
            .singleOrError())
        .flatMap(response -> mapCreateStoreResponse(response));
  }

  @Override public Single<Account> saveAutoLoginCredentials(AutoLoginCredentials credentials) {
    authenticationProvider.saveAuthentication(credentials.getAccessToken(),
        credentials.getRefreshToken());
    return serviceV7.getUserInfo(
        new AccountRequestBody(Collections.singletonList("meta"), credentials.getAccessToken()))
        .singleOrError()
        .flatMap(response -> {
          if (response.isSuccessful() && response.body()
              .isOk()) {
            return Single.just(mapper.map(response.body(), BaseAccount.LoginType.APTOIDE));
          }
          return Single.just(AccountFactory.of(false, false, null, BaseAccount.LoginType.APTOIDE));
        });
  }

  @Override public void removeAccessTokenFromPersistence() {
    authenticationProvider.removeAuthentication();
  }

  @Override public Single<CodeAuth> sendMagicLink(String email) {
    return aptoideAuthentication.sendMagicLink(email);
  }

  @Override
  public Single<Account> getAccount(String email, String code, String state, String agent) {
    return aptoideAuthentication.authenticate(code, state, agent)
        .flatMap(oAuth2 -> {
          authenticationProvider.saveAuthentication(oAuth2.getData()
              .getAccessToken(), oAuth2.getData()
              .getRefreshToken());
          return serviceV7.getUserInfo(new AccountRequestBody(Collections.singletonList("meta"),
              oAuth2.getData()
                  .getAccessToken()))
              .singleOrError();
        })
        .flatMap(response -> {
          if (response.isSuccessful() && response.body()
              .isOk()) {
            return Single.just(mapper.map(response.body(), BaseAccount.LoginType.APTOIDE));
          }
          return Single.error(new IllegalStateException(response.message()));
        });
  }

  private Single<CreateStoreStatus> mapCreateStoreResponse(Response<CreateStoreResponse> response) {
    if (response.isSuccessful() && response.body()
        .getStatus()
        .equals(Status.OK)) {
      return Single.just(new CreateStoreStatus(response.body()
          .getStatus(), response.body()
          .getErrors()));
    } else if (response.body()
        .getStatus()
        .equals(Status.FAIL)) {
      if (response.body()
          .getErrors()
          .get(0)
          .getCode()
          .equals("WOP-3")) {
        return Single.error(new DuplicatedStoreException());
      }
    }
    return Single.error(new IOException());
  }

  public interface ServiceV3 {
    @POST("webservices/3/checkUserCredentials") @FormUrlEncoded
    Observable<Response<CreateStoreResponse>> createRepo(@Field("repo") String storeName,
        @Field("privacy_user") String privacyUser, @Field("privacy_pass ") String privacyPass,
        @Field("privacy") boolean privacyFlag, @Field("createRepo") int createRepo,
        @Field("oauthCreateRepo") boolean oauthCreateRepo, @Field("oauthToken") String oauthtoken,
        @Field("authMode") String authMode, @Field("access_token") String accessToken,
        @Field("mode") String mode);
  }

  public interface ServiceV7 {
    @POST("user/get") Observable<Response<AccountResponse>> getUserInfo(
        @Body AccountRequestBody body);
  }
}
