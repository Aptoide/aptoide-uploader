package com.aptoide.uploader.account;

import com.aptoide.uploader.FirstLaunchPersistence;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class AptoideAccountManager {

  private final AccountService accountService;
  private final AccountPersistence accountPersistence;
  private final CredentialsValidator credentialsValidator;
  private final VanillaLoginProvider vanillaLoginProvider;
  private final FirstLaunchPersistence firstLaunchPersistence;

  public AptoideAccountManager(AccountService accountService, AccountPersistence accountPersistence,
      CredentialsValidator credentialsValidator, VanillaLoginProvider vanillaLoginProvider,
      FirstLaunchPersistence firstLaunchPersistence) {
    this.accountService = accountService;
    this.accountPersistence = accountPersistence;
    this.credentialsValidator = credentialsValidator;
    this.vanillaLoginProvider = vanillaLoginProvider;
    this.firstLaunchPersistence = firstLaunchPersistence;
  }

  public Completable login(String username, String password) {
    return accountService.getAccount(username, password)
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Observable<AptoideAccount> getAccount() {
    return accountPersistence.getAccount()
        .flatMap(aptoideAccount -> {
          if (!aptoideAccount.isLoggedIn() && firstLaunchPersistence.isFirstLaunch()) {
            return vanillaLoginProvider.getAccount()
                .flatMap(account -> accountPersistence.save(account)
                    .toObservable());
          } else {
            return Observable.just(aptoideAccount);
          }
        });
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