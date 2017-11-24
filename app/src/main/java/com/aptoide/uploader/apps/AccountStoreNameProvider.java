package com.aptoide.uploader.apps;

import com.aptoide.uploader.account.AptoideAccountManager;
import io.reactivex.Single;

/**
 * Created by jdandrade on 24/11/2017.
 */

public class AccountStoreNameProvider implements StoreNameProvider {
  private final AptoideAccountManager accountManager;

  public AccountStoreNameProvider(AptoideAccountManager accountManager) {
    this.accountManager = accountManager;
  }

  @Override public Single<String> getStoreName() {
    return accountManager.getAccount()
        .firstOrError()
        .flatMap(aptoideAccount -> {
          if (aptoideAccount.hasStore()) {
            return Single.just(aptoideAccount.getStoreName());
          }
          return Single.error(new IllegalAccessError("Store must have a name"));
        });
  }
}
