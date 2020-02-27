package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.Category;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.GET;

public class RetrofitCategoriesService {

  private final RetrofitCategoriesService.ServiceV7 serviceV7;

  public RetrofitCategoriesService(RetrofitCategoriesService.ServiceV7 serviceV7) {
    this.serviceV7 = serviceV7;
  }

  public Single<CategoriesModel> getCategories() {
    return serviceV7.getCategories()
        .map(getCategoriesResponse -> {
          if (getCategoriesResponse.isSuccessful()) {
            return mapToCategoriesList(getCategoriesResponse.body(), CategoriesModel.Status.OK);
          } else {
            return new CategoriesModel(null, CategoriesModel.Status.FAIL);
          }
        })
        .subscribeOn(Schedulers.io());
  }

  private CategoriesModel mapToCategoriesList(GetCategoriesResponse getCategoriesResponse,
      CategoriesModel.Status status) {
    List<Category> list = new ArrayList<>();
    CategoriesModel categoriesModel = new CategoriesModel(list, status);
    for (GetCategoriesResponse.Data category : getCategoriesResponse.getDatalist()
        .getList()) {
      list.add(new Category(category.getId(), category.getTitle()));
    }
    return categoriesModel;
  }

  public interface ServiceV7 {
    @GET("apks/groups/get/groups_depth=1/limit=65")
    Single<Response<GetCategoriesResponse>> getCategories();
  }
}
