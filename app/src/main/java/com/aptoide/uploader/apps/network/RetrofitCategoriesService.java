package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.apps.Category;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class RetrofitCategoriesService {

  private final RetrofitCategoriesService.ServiceV7 serviceV7;

  public RetrofitCategoriesService(RetrofitCategoriesService.ServiceV7 serviceV7) {
    this.serviceV7 = serviceV7;
  }

  public Single<CategoriesResponse> getCategories() {
    return serviceV7.getCategories()
        .map(getCategoriesResponse -> {
          if (getCategoriesResponse.isSuccessful()) {
            return mapToCategoriesList(getCategoriesResponse.body(), CategoriesResponse.Status.OK);
          } else {
            return new CategoriesResponse(null, CategoriesResponse.Status.FAIL);
          }
        })
        .subscribeOn(Schedulers.io());
  }

  private CategoriesResponse mapToCategoriesList(GetCategoriesResponse getCategoriesResponse,
      CategoriesResponse.Status status) {
    List<Category> list = new ArrayList<>();
    CategoriesResponse categoriesResponse = new CategoriesResponse(list, status);
    for (GetCategoriesResponse.Data category : getCategoriesResponse.getDatalist()
        .getList()) {
      list.add(new Category(category.getId(), category.getName(), category.getTitle(),
          category.getIcon(), category.getGraphic(), category.getAdded(), category.getModified(),
          category.getParent(), category.getStats()));
    }
    return categoriesResponse;
  }

  public interface ServiceV7 {
    @GET("apks/groups/get/groups_depth=1") Single<Response<GetCategoriesResponse>> getCategories();
  }

  //private class Body {
//
  //  private final int offset;
  //  private final String language;
//
  //  private Body(int offset, String language) {
  //    this.offset = offset;
  //    this.language = language;
  //  }
  //}
}
