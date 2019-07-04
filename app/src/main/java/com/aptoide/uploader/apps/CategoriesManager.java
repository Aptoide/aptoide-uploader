package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.CategoriesResponse;
import com.aptoide.uploader.apps.network.RetrofitCategoriesService;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;

public class CategoriesManager {

  private final RetrofitCategoriesService retrofitCategoriesService;

  public CategoriesManager(RetrofitCategoriesService retrofitCategoriesService) {
    this.retrofitCategoriesService = retrofitCategoriesService;
  }

  public Single<List<Category>> getCategories() {
    return retrofitCategoriesService.getCategories()
        .map(getCategoriesResponse -> mapToCategoriesName(getCategoriesResponse));
  }

  private List<Category> mapToCategoriesName(CategoriesResponse categoriesResponse) {
    return categoriesResponse.getList();
  }
}
