package com.aptoide.uploader.account;

public class GoogleAccount extends Account {

  private String storeType = "Google";

  public GoogleAccount(boolean hasStore, boolean loggedIn, String storeName) {
    super(hasStore, loggedIn, storeName);
  }

  public String getStoreType() {
    return storeType;
  }
}
