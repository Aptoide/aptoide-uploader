package com.aptoide.uploader.account;

import com.aptoide.authentication.model.CodeAuth;
import com.aptoide.uploader.account.network.CreateStoreStatus;
import io.reactivex.Single;

/**
 * Created by jdandrade on 03/11/2017.
 */

public interface AccountService {

  Single<Account> getAccount(String email, String ServerAuthToken, String authMode);

  Single<CreateStoreStatus> createStore(String storeName, String privateUserName, String privatePassword);

  Single<Account> saveAutoLoginCredentials(AutoLoginCredentials credentials);

  void removeAccessTokenFromPersistence();

  Single<CodeAuth> sendMagicLink(String email);

  Single<Account> getAccount(String email, String code, String state, String agent);
}
