package com.aptoide.uploader.account;

public class AccountFactory {

  public static Account of(boolean hasStore, boolean isLoggedIn, String getStoreName,
      BaseAccount.LoginType loginType, String avatarPath) {
    switch (loginType) {
      case APTOIDE:
        return new AptoideAccount(hasStore, isLoggedIn, getStoreName, avatarPath);
      case GOOGLE:
        return new GoogleAccount(hasStore, isLoggedIn, getStoreName, avatarPath);
      case FACEBOOK:
        return new FacebookAccount(hasStore, isLoggedIn, getStoreName, avatarPath);
    }
    return new AptoideAccount(hasStore, isLoggedIn, getStoreName, avatarPath);
  }
}
