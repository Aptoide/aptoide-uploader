package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.CategoriesModel;
import com.aptoide.uploader.apps.network.RetrofitCategoriesService;
import io.reactivex.Single;
import java.util.List;

public class CategoriesManager {

  private final RetrofitCategoriesService retrofitCategoriesService;

  public CategoriesManager(RetrofitCategoriesService retrofitCategoriesService) {
    this.retrofitCategoriesService = retrofitCategoriesService;
  }

  public Single<List<Category>> getCategories() {
    return retrofitCategoriesService.getCategories()
        .map(getCategoriesModel -> mapToCategoriesName(getCategoriesModel));
  }

  private List<Category> mapToCategoriesName(CategoriesModel categoriesModel) {
    return categoriesModel.getList();
  }
}
