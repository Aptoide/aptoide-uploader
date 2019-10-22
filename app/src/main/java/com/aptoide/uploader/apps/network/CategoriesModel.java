package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.Category;
import java.util.List;

public class CategoriesModel {

  private final List<Category> list;
  private final Status status;

  public List<Category> getList() {
    return list;
  }

  public Status getStatus() {
    return status;
  }

  public CategoriesModel(List<Category> list, Status status) {

    this.list = list;
    this.status = status;
  }

  public enum Status{
    OK, QUEUED, FAIL
  }

}
