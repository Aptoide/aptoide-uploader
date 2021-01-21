package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface AutoUploadView extends View {
  void showApps(@NotNull List<InstalledApp> appsList);

  void refreshApps(@NotNull List<InstalledApp> appsList);

  Observable<Boolean> refreshEvent();

  Single<List<InstalledApp>> getSelectedApps();
}
