package com.aptoide.uploader.account;

public class FacebookAccount extends Account {

  private String storeType = "Facebook";

  public FacebookAccount(boolean hasStore, boolean loggedIn, String storeName) {
    super(hasStore, loggedIn, storeName);
  }

  public String getStoreType() {
    return storeType;
  }
}
