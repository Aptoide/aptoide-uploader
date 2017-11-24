package com.aptoide.uploader.account;

public class AptoideAccount {

  private final boolean hasStore;
  private boolean loggedIn;

  public AptoideAccount(boolean hasStore, boolean loggedIn) {
    this.hasStore = hasStore;
    this.loggedIn = loggedIn;
  }

  public boolean hasStore() {
    return hasStore;
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }
}
