package com.aptoide.uploader.account;

public class GoogleAccount extends BaseAccount {

  public GoogleAccount(boolean hasStore, boolean loggedIn, String storeName) {
    super(hasStore, loggedIn, storeName);
  }

  @Override public LoginType getLoginType() {
    return LoginType.GOOGLE;
  }
}
