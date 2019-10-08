package com.aptoide.uploader.account;

public class AptoideAccount extends Account {

  private String storeType = "Aptoide";

  public AptoideAccount(boolean hasStore, boolean loggedIn, String storeName) {
    super(hasStore, loggedIn, storeName);
  }

  public String getStoreType() {
    return storeType;
  }
}
