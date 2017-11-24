package com.aptoide.uploader.account;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class AptoideAccountManager {

  private final AccountService accountService;
  private final AccountPersistence accountPersistence;

  public AptoideAccountManager(AccountService accountService,
      AccountPersistence accountPersistence) {
    this.accountService = accountService;
    this.accountPersistence = accountPersistence;
  }

  public Completable login(String username, String password) {
    return accountService.getAccount(username, password)
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Observable<AptoideAccount> getAccount() {
    return accountPersistence.getAccount();
  }
}
