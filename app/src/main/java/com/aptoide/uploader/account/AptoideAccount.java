package com.aptoide.uploader.account;

/**
 * Created by jdandrade on 03/11/2017.
 */

public class AptoideAccount {

  private final boolean hasStore;

  public AptoideAccount(boolean hasStore) {
    this.hasStore = hasStore;
  }

  public boolean hasStore() {
    return hasStore;
  }
}
