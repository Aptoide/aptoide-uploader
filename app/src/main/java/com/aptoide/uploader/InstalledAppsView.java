package com.aptoide.uploader;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Created by pedroribeiro on 10/11/17.
 */

public interface InstalledAppsView extends View {
  void showApps(@NotNull List<App> appsList);
}
