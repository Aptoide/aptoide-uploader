package com.aptoide.uploader.upload;

import com.aptoide.uploader.account.AptoideAccount;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.security.AuthenticationProvider;
import io.reactivex.Single;

public class AptoideAccountProvider implements AccountProvider {
  private final AptoideAccountManager accountManager;
  private final AuthenticationProvider authenticationProvider;

  public AptoideAccountProvider(AptoideAccountManager accountManager,
      AuthenticationProvider authenticationProvider) {
    this.accountManager = accountManager;
    this.authenticationProvider = authenticationProvider;
  }

  @Override public Single<AptoideAccount> getAccount() {
    return accountManager.getAccount()
        .firstOrError();
  }

  @Override public Single<String> getToken() {
    return authenticationProvider.getAccessToken();
  }
}
