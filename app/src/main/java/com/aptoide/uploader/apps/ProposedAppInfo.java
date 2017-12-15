package com.aptoide.uploader.apps;

public class ProposedAppInfo {
  private final String appName;
  private final int appCategory;
  private final String language;

  public ProposedAppInfo() {
    appName = null;
    language = null;
    appCategory = 0;
  }

  public ProposedAppInfo(String appName, int appCategory, String language) {
    this.appName = appName;
    this.appCategory = appCategory;
    this.language = language;
  }

  public boolean hasData() {
    return appName != null && language != null;
  }

  public String getAppName() {
    return appName;
  }

  public int getAppCategory() {
    return appCategory;
  }

  public String getLanguage() {
    return language;
  }
}
