package com.aptoide.uploader.account;

public class AccountFactory {

  public static Account of(boolean hasStore, boolean isLoggedIn, String getStoreName,
      BaseAccount.LoginType loginType) {
    switch (loginType) {
      case APTOIDE:
        return new AptoideAccount(hasStore, isLoggedIn, getStoreName);
      case GOOGLE:
        return new GoogleAccount(hasStore, isLoggedIn, getStoreName);
      case FACEBOOK:
        return new FacebookAccount(hasStore, isLoggedIn, getStoreName);
    }
    return new AptoideAccount(hasStore, isLoggedIn, getStoreName);
  }
}
