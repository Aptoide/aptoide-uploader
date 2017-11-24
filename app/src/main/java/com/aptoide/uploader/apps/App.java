package com.aptoide.uploader.apps;

public class App {

  private final String icon;
  private final String name;
  private final boolean isSystem;

  public App(String icon, String name, boolean isSystem) {
    this.icon = icon;
    this.name = name;
    this.isSystem = isSystem;
  }

  public String getIcon() {
    return icon;
  }

  public String getName() {
    return name;
  }

  public boolean isSystem() {
    return isSystem;
  }
}
