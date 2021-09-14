package com.aptoide.uploader.account;

public class GoogleAccount extends BaseAccount {

  public GoogleAccount(boolean hasStore, boolean loggedIn, String storeName, String avatarPath) {
    super(hasStore, loggedIn, storeName, avatarPath);
  }

  @Override public LoginType getLoginType() {
    return LoginType.GOOGLE;
  }
}
