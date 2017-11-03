package com.aptoide.uploader;

import io.reactivex.Completable;

public class AptoideAccountManager {

  private final AccountService accountService;

  public AptoideAccountManager(AccountService accountService) {
    this.accountService = accountService;
  }

  public Completable login(String username, String password) {
    return accountService.getAccount(username, password)
        .toCompletable();
  }
}
