package com.aptoide.uploader.apps;

import java.util.List;

public class Store {

  private final String name;
  private final List<InstalledApp> apps;

  Store(String name, List<InstalledApp> apps) {
    this.name = name;
    this.apps = apps;
  }

  public String getName() {
    return name;
  }

  public List<InstalledApp> getApps() {
    return apps;
  }
}
