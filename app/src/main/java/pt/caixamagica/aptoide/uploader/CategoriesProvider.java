package pt.caixamagica.aptoide.uploader;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import pt.caixamagica.aptoide.uploader.retrofit.request.CategoriesRequest;
import pt.caixamagica.aptoide.uploader.webservices.json.CategoriesResponse;

/**
 * Created by filipe on 22-11-2017.
 */

public class CategoriesProvider {

  private SpiceManager spiceManager;
  private int totalCategories = 0;
  private int count = 0;
  private List<String> categoriesNamesList;
  private List<CategoriesResponse.DataList.List> categories;
  private CategoriesProviderListener listener;

  public CategoriesProvider(SpiceManager spiceManager) {
    this.spiceManager = spiceManager;
    categoriesNamesList = new LinkedList<>();
    categories = new ArrayList<>();
  }

  private void requestCategories(final CategoriesProviderListener listener) {
    int offset = 0;
    offset += count;
    final CategoriesRequest categoriesRequest = new CategoriesRequest(offset);

    spiceManager.execute(categoriesRequest, new RequestListener<CategoriesResponse>() {
      @Override public void onRequestFailure(SpiceException spiceException) {
        spiceException.printStackTrace();
        listener.onErrorProvidingCategories();
      }

      @Override public void onRequestSuccess(CategoriesResponse categoriesResponseJson) {

        int total = (int) categoriesResponseJson.datalist.getTotal();
        int countCategories = (int) categoriesResponseJson.datalist.getCount();

        updateCounters(total, countCategories);

        addNewCategoriesList(categoriesResponseJson.datalist.getList());

        if (count < totalCategories) {
          requestCategories(listener);
        } else {
          listener.onAllCategoriesProvided(categoriesNamesList);
        }
      }
    });
  }

  private void addNewCategoriesList(List<CategoriesResponse.DataList.List> list) {

    categories.addAll(list);

    Collections.sort(categories, new Comparator<CategoriesResponse.DataList.List>() {

      @Override public int compare(CategoriesResponse.DataList.List lhs,
          CategoriesResponse.DataList.List rhs) {
        return lhs.getTitle()
            .compareTo(rhs.getTitle());
      }
    });

    for (CategoriesResponse.DataList.List category : categories) {
      if (!categoriesNamesList.contains(category.getTitle())) {
        categoriesNamesList.add(category.getTitle());
      }
    }
  }

  private void updateCounters(int total, int countCategories) {
    totalCategories = total;
    count += countCategories;
  }

  public void getCategoriesNamesList(CategoriesProviderListener listener) {
    this.listener = listener;
    requestCategories(this.listener);
  }

  public void removeListener() {
    this.listener = null;
  }

  /**
   * Retorna o id da categoria cujo nome é <code>name</code>, -1 para default (não encontrado).
   *
   * @param name nome da categoria
   *
   * @return o id da categoria cujo nome é <code>name</code>.
   */
  public int getCategoryId(String name) {

    // Default Value
    if (name.equals("App Category (Optional)")) return -1;

    for (CategoriesResponse.DataList.List category : categories) {
      if (category.getTitle()
          .equals(name)) {
        return category.getId()
            .intValue();
      }
    }

    return -1;
  }

  /**
   * Retorna o indice no spinner da categoria com o <code>id</code> fornecido. 0 caso seja
   * desconhecido.
   *
   * @param id id da categoria
   *
   * @return o indice no spinner da categoria com o <code>id</code> fornecido.
   */
  public int getCategorySpinnerIndex(Number id) {

    int i = 0;
    for (CategoriesResponse.DataList.List category : categories) {
      i++;
      if (category.getId()
          .equals(id)) {
        return i;
      }
    }
    return 0;
  }

  public int idFromCategoryName(String name) {
    for (CategoriesResponse.DataList.List category : categories) {
      if (category.getTitle()
          .equals(name)) {
        return (int) category.getId();
      }
    }
    return 0;
  }
}