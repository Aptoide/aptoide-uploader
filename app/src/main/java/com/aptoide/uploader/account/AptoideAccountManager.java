package com.aptoide.uploader.account;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class AptoideAccountManager {

  private final AccountService accountService;
  private final AccountPersistence accountPersistence;
  private final CredentialsValidator credentialsValidator;

  public AptoideAccountManager(AccountService accountService, AccountPersistence accountPersistence,
      CredentialsValidator credentialsValidator) {
    this.accountService = accountService;
    this.accountPersistence = accountPersistence;
    this.credentialsValidator = credentialsValidator;
  }

  public Completable login(String username, String password) {
    return accountService.getAccount(username, password)
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Observable<AptoideAccount> getAccount() {
    return accountPersistence.getAccount();
  }

  public Completable create(String email, String password, String storeName) {
    return credentialsValidator.validate(email, password, storeName)
        .andThen(accountService.createAccount(email, password, storeName))
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Completable logout() {
    return accountPersistence.remove();
  }
}