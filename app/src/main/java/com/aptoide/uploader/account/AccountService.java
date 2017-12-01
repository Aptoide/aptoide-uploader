package com.aptoide.uploader.account;

import io.reactivex.Single;

/**
 * Created by jdandrade on 03/11/2017.
 */

public interface AccountService {

  Single<AptoideAccount> getAccount(String username, String password);

  Single<AptoideAccount> createAccount(String email, String password, String storeName);

  Single<AptoideAccount> createAccount(String email, String password, String storeName,
      String storeUser, String storePass);
}
