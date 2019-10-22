package com.aptoide.uploader.account;

public class FacebookAccount extends BaseAccount {

  public FacebookAccount(boolean hasStore, boolean loggedIn, String storeName) {
    super(hasStore, loggedIn, storeName);
  }

  @Override public LoginType getLoginType() {
    return LoginType.FACEBOOK;
  }
}
