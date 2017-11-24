package com.aptoide.uploader.apps;

import java.util.List;

/**
 * Created by jdandrade on 24/11/2017.
 */

public class Store {

  private final String name;
  private final List<App> apps;

  Store(String name, List<App> apps) {
    this.name = name;
    this.apps = apps;
  }

  public String getName() {
    return name;
  }

  public List<App> getApps() {
    return apps;
  }
}
