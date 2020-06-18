package com.aptoide.uploader.account;

import com.aptoide.authentication.model.CodeAuth;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class AptoideAccountManager {

  private final AccountService accountService;
  private final AccountPersistence accountPersistence;
  private final CredentialsValidator credentialsValidator;
  private final SocialLogoutManager socialLogoutManager;
  private final AutoLoginPersistence autoLoginPersistence;

  public AptoideAccountManager(AccountService accountService, AccountPersistence accountPersistence,
      CredentialsValidator credentialsValidator, SocialLogoutManager socialLogoutManager,
      AutoLoginPersistence autoLoginPersistence) {
    this.accountService = accountService;
    this.accountPersistence = accountPersistence;
    this.credentialsValidator = credentialsValidator;
    this.socialLogoutManager = socialLogoutManager;
    this.autoLoginPersistence = autoLoginPersistence;
  }

  public Completable login(AptoideCredentials aptoideCredentials) {
    return credentialsValidator.validate(aptoideCredentials)
        .andThen(
            accountService.getAccount(aptoideCredentials.getEmail(), aptoideCredentials.getCode(),
                aptoideCredentials.getState(), aptoideCredentials.getAgent()))
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Completable loginWithGoogle(String email, String serverAuthToken) {
    return accountService.getAccount(email, serverAuthToken, "google")
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Completable loginWithFacebook(String email, String serverAuthToken) {
    return accountService.getAccount(email, serverAuthToken, "facebook_uploader")
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Single<CodeAuth> sendMagicLink(String email) {
    return accountService.sendMagicLink(email);
  }

  public Single<Boolean> isEmailValid(String email) {
    return credentialsValidator.isEmailValid(email);
  }

  public Observable<Account> getAccount() {
    return accountPersistence.getAccount();
  }

  public Completable createStore(String storeName) {
    return accountService.createStore(storeName)
        .flatMap(createStoreStatus -> getAccount().firstOrError())
        .map(newAccount -> AccountFactory.of(true, true, storeName, newAccount.getLoginType()))
        .flatMapCompletable(account -> accountPersistence.save(account));
  }

  public Observable<Account> saveAutoLoginCredentials(AutoLoginCredentials credentials) {
    return accountService.saveAutoLoginCredentials(credentials)
        .toObservable();
  }

  public Completable loginWithAutoLogin(Account account) {
    return accountPersistence.save(account);
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
        .doOnError(throwable -> throwable.printStackTrace())
        .firstOrError()
        .flatMapCompletable(account -> accountPersistence.remove())
        .doOnComplete(() -> autoLoginPersistence.setForcedLogout(true));
  }

  public void removeAccessTokenFromPersistence() {
    accountService.removeAccessTokenFromPersistence();
  }
}