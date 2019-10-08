package com.aptoide.uploader.upload;

import com.aptoide.uploader.account.Account;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.security.AuthenticationProvider;
import io.reactivex.Observable;
import io.reactivex.Single;

public class AptoideAccountProvider implements AccountProvider {
  private final AptoideAccountManager accountManager;
  private final AuthenticationProvider authenticationProvider;

  public AptoideAccountProvider(AptoideAccountManager accountManager,
      AuthenticationProvider authenticationProvider) {
    this.accountManager = accountManager;
    this.authenticationProvider = authenticationProvider;
  }

  @Override public Observable<Account> getAccount() {
    return accountManager.getAccount();
  }

  @Override public Single<String> getToken() {
    return authenticationProvider.getAccessToken();
  }

  @Override public Single<String> revalidateAccessToken() {
    return authenticationProvider.getNewAccessToken();
  }

  @Override public Single<String> getRefreshToken() {
    return authenticationProvider.getRefreshToken();
  }
}
