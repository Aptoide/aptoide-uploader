package com.aptoide.uploader.account;

public class AptoideAccount {

  private final boolean hasStore;
  private final boolean loggedIn;
  private final String storeName;

  public AptoideAccount(boolean hasStore, boolean loggedIn, String storeName) {
    this.hasStore = hasStore;
    this.loggedIn = loggedIn;
    this.storeName = storeName;
  }

  public boolean hasStore() {
    return hasStore;
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  public String getStoreName() {
    return storeName;
  }
}
