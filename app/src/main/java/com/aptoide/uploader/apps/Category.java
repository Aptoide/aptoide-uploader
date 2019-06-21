package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.GetCategoriesResponse;

public class Category {

  private int id;
  private String name;
  private String title;
  private String icon;
  private String graphic;
  private String added;
  private String modified;
  private GetCategoriesResponse.Parent parent;
  private GetCategoriesResponse.Stats stats;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getGraphic() {
    return graphic;
  }

  public void setGraphic(String graphic) {
    this.graphic = graphic;
  }

  public String getAdded() {
    return added;
  }

  public void setAdded(String added) {
    this.added = added;
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public void setParent(GetCategoriesResponse.Parent parent) {
    this.parent = parent;
  }

  public GetCategoriesResponse.Stats getStats() {
    return stats;
  }

  public void setStats(GetCategoriesResponse.Stats stats) {
    this.stats = stats;
  }

  public Category(int id, String name, String title, String icon, String graphic,
      String added, String modified, GetCategoriesResponse.Parent parent,
      GetCategoriesResponse.Stats stats) {

    this.id = id;
    this.name = name;
    this.title = title;
    this.icon = icon;
    this.graphic = graphic;
    this.added = added;
    this.modified = modified;
    this.parent = parent;
    this.stats = stats;
  }

  public GetCategoriesResponse.Parent getParent() { return parent; }

  public static class Parent {
    private int id;
    private String name;
    private String title;
    private String icon;
    private String graphic;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getIcon() {
      return icon;
    }

    public void setIcon(String icon) {
      this.icon = icon;
    }

    public String getGraphic() {
      return graphic;
    }

    public void setGraphic(String graphic) {
      this.graphic = graphic;
    }
  }

  public static class Stats {
    private int groups;
    private int items;
  }
}
