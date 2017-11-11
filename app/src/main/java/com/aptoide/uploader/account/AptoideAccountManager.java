package com.aptoide.uploader.account;

import io.reactivex.Single;

public class AptoideAccountManager {

  private final AccountService accountService;

  public AptoideAccountManager(AccountService accountService) {
    this.accountService = accountService;
  }

  public Single<AptoideAccount> login(String username, String password) {
    return accountService.getAccount(username, password);
  }
}
