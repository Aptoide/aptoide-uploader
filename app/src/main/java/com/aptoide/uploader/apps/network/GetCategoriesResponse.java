package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.Error;
import com.aptoide.uploader.account.network.ResponseV7;
import java.util.List;

public class GetCategoriesResponse extends ResponseV7 {

  public DataList datalist;

  public GetCategoriesResponse(Info info, List<Error> errors) {
    super(info, errors);
  }

  public DataList getDatalist() {
    return datalist;
  }

  public static class DataList {
    private int total;
    private int count;
    private int offset;
    private int limit;
    private int next;
    private int hidden;
    private boolean loaded;
    private List<Data> list;

    public List<Data> getList() {
      return list;
    }
  }

  public static class Data {
    private String name;
    private String title;
    private String icon;
    private String graphic;
    private String added;
    private String modified;
    private Parent parent;
    private Stats stats;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    private int id;

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

    public Parent getParent() {
      return parent;
    }

    public void setParent(Parent parent) {
      this.parent = parent;
    }

    public Stats getStats() {
      return stats;
    }

    public void setStats(Stats stats) {
      this.stats = stats;
    }
  }

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

    public int getGroups() {
      return groups;
    }

    public void setGroups(int groups) {
      this.groups = groups;
    }

    public int getItems() {
      return items;
    }

    public void setItems(int items) {
      this.items = items;
    }
  }
}
