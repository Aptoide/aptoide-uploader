package com.aptoide.uploader.account;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class AptoideAccountManager {

  private final AccountService accountService;
  private final AccountPersistence accountPersistence;
  private final CredentialsValidator credentialsValidator;
  private final SocialLogoutManager socialLogoutManager;

  public AptoideAccountManager(AccountService accountService, AccountPersistence accountPersistence,
      CredentialsValidator credentialsValidator, SocialLogoutManager socialLogoutManager) {
    this.accountService = accountService;
    this.accountPersistence = accountPersistence;
    this.credentialsValidator = credentialsValidator;
    this.socialLogoutManager = socialLogoutManager;
  }

  public Completable login(String username, String password) {
    return accountService.getAccount(username, password)
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Completable loginWithGoogle(String email, String token) {
    return accountService.getAccountOAuth(email, token, "google")
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Completable loginWithFacebook(String email, String token) {
    return accountService.getAccountOAuth(email, token, "facebook_uploader")
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Observable<Account> getAccount() {
    return accountPersistence.getAccount();
  }

  public Completable create(String email, String password, String storeName) {
    return credentialsValidator.validate(email, password, storeName)
        .andThen(accountService.createAccount(email, password, storeName))
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Completable createStore(String storeName) {
    return accountService.createStore(storeName)
        .flatMap(createStoreStatus -> getAccount().firstOrError())
        .map(newAccount -> AccountFactory.of(true, true, storeName, newAccount.getLoginType()))
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Completable logout() {
    return accountPersistence.getAccount()
        .doOnNext(account -> {
          if (account.getLoginType()
              .equals(BaseAccount.LoginType.GOOGLE) || account.getLoginType()
              .equals(BaseAccount.LoginType.FACEBOOK)) {
            socialLogoutManager.handleSocialLogout(account.getLoginType());
          }
        })
        .firstOrError()
        .flatMapCompletable(account -> accountPersistence.remove());
  }
}