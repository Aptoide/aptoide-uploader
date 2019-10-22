package com.aptoide.uploader.apps;

public class Category {

  private int id;
  private String title;

  public int getId() {
    return id;
  }

  public Category(int id, String title) {

    this.id = id;
    this.title = title;
  }

  @Override public String toString() {
    return title;
  }
}
