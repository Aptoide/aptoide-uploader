package com.aptoide.uploader.apps;

public class App {

  private final String icon;
  private final String name;
  private final boolean isSystem;
  private boolean isSelected;

  public App(String icon, String name, boolean isSystem, boolean isSelected) {
    this.icon = icon;
    this.name = name;
    this.isSystem = isSystem;
    this.isSelected = isSelected;
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

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
  }
}
