package com.aptoide.uploader.apps;

public class App {

  private final String icon;
  private final String name;
  private final boolean isSystem;
  private final String packageName;

  public App(String icon, String name, boolean isSystem, String packageName) {
    this.icon = icon;
    this.name = name;
    this.isSystem = isSystem;
    this.packageName = packageName;
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

  public String getPackageName() {
    return packageName;
  }

}
