package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.App;
import com.aptoide.uploader.view.View;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Created by pedroribeiro on 10/11/17.
 */

public interface MyAppsView extends View {
  void showApps(@NotNull List<App> appsList);

  void showStoreName(@NotNull String storeName);
}
