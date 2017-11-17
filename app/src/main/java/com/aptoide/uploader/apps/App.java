package com.aptoide.uploader.apps;

/**
 * Created by pedroribeiro on 10/11/17.
 */

public class App {

  private final String icon;
  private final String name;

  public App(String icon, String name) {
    this.icon = icon;
    this.name = name;
  }

  public String getIcon() {
    return icon;
  }

  public String getName() {
    return name;
  }
}
