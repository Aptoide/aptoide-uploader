package pt.caixamagica.aptoide.uploader;

import java.util.List;

/**
 * Created by filipe on 23-11-2017.
 */

public interface CategoriesProviderListener {

  void onAllCategoriesProvided(List<String> categories);

  void onErrorProvidingCategories();
}
