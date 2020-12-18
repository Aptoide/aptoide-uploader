package com.aptoide.uploader.account;

public class FacebookAccount extends BaseAccount {

  public FacebookAccount(boolean hasStore, boolean loggedIn, String storeName, String avatarPath) {
    super(hasStore, loggedIn, storeName, avatarPath);
  }

  @Override public LoginType getLoginType() {
    return LoginType.FACEBOOK;
  }
}
