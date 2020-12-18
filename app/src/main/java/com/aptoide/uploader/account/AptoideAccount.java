package com.aptoide.uploader.account;

public class AptoideAccount extends BaseAccount {

  public AptoideAccount(boolean hasStore, boolean loggedIn, String storeName, String avatarPath) {
    super(hasStore, loggedIn, storeName, avatarPath);
  }

  @Override public LoginType getLoginType() {
    return LoginType.APTOIDE;
  }
}
