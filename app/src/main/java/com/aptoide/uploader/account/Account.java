package com.aptoide.uploader.account;

public interface Account {

  boolean hasStore();

  boolean isLoggedIn();

  String getStoreName();

  String getAvatarPath();

  BaseAccount.LoginType getLoginType();
}
