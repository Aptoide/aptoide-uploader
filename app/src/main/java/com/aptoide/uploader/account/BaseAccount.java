package com.aptoide.uploader.account;

public abstract class BaseAccount implements Account {

  private final boolean hasStore;
  private boolean loggedIn;
  private final String storeName;
  private final String avatarPath;
  private boolean forcedLogout;

  public BaseAccount(boolean hasStore, boolean loggedIn, String storeName, String avatarPath) {
    this.hasStore = hasStore;
    this.loggedIn = loggedIn;
    this.storeName = storeName;
    this.avatarPath = avatarPath;
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

  public String getAvatarPath() {
    return avatarPath;
  }

  public enum LoginType {
    FACEBOOK("FACEBOOK"), GOOGLE("GOOGLE"), APTOIDE("APTOIDE"), NONE("NONE");

    private String text;

    LoginType(String text) {
      this.text = text;
    }

    public String getText() {
      return this.text;
    }
  }
}
