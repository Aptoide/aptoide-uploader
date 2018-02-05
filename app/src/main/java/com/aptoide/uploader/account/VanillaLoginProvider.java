package com.aptoide.uploader.account;

import com.aptoide.uploader.security.AuthenticationPersistance;
import com.aptoide.uploader.security.VanillaContentProviderParser;
import io.reactivex.Observable;

/**
 * Created by filipe on 08-01-2018.
 */

public class VanillaLoginProvider {

  private final AccountPersistence accountPersistence;
  private final AuthenticationPersistance authenticationPersistance;
  private final VanillaContentProviderParser vanillaContentProviderParser;

  public VanillaLoginProvider(AccountPersistence accountPersistence,
      AuthenticationPersistance authenticationPersistance,
      VanillaContentProviderParser vanillaContentProviderParser) {
    this.accountPersistence = accountPersistence;
    this.authenticationPersistance = authenticationPersistance;
    this.vanillaContentProviderParser = vanillaContentProviderParser;
  }

  public String getStoreName() {
    return vanillaContentProviderParser.getStoreName();
  }

  public String getRefreshToken() {
    return vanillaContentProviderParser.getRefreshToken();
  }

  public String getAccessToken() {
    return vanillaContentProviderParser.getAccessToken();
  }

  public Observable<AptoideAccount> getAccount() {
    String storeName = getStoreName();
    String accessToken = getAccessToken();
    String refreshToken = getRefreshToken();

    if (!storeName.isEmpty() && !accessToken.isEmpty() && !refreshToken.isEmpty()) {
      AptoideAccount account = new AptoideAccount(true, true, storeName);
      storeAccount(account);
      storeTokens(accessToken, refreshToken);
      return Observable.just(account);
    }
    return Observable.just(new AptoideAccount(false, false, null));
  }

  private void storeAccount(AptoideAccount account) {
    accountPersistence.save(account);
  }

  private void storeTokens(String accessToken, String refreshToken) {
    authenticationPersistance.saveAuthentication(accessToken, refreshToken);
  }
}
