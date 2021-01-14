package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.InstalledApp;
import io.reactivex.Single;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface AutoUploadView {
  void showApps(@NotNull List<InstalledApp> appsList);

  void refreshApps(@NotNull List<InstalledApp> appsList);

  Single<List<InstalledApp>> getSelectedApps();
}
