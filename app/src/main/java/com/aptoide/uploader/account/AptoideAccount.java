package com.aptoide.uploader.account;

public class AptoideAccount extends BaseAccount {

  public AptoideAccount(boolean hasStore, boolean loggedIn, String storeName) {
    super(hasStore, loggedIn, storeName);
  }

  @Override public LoginType getLoginType() {
    return LoginType.APTOIDE;
  }
}
