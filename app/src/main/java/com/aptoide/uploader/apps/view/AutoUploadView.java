package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.AutoUploadSelects;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface AutoUploadView extends View {
  public void showError();

  Observable<Object> backToSettingsClick();

  void showApps(@NotNull List<InstalledApp> appsList, List<AutoUploadSelects> selectsList);

  void refreshApps(@NotNull List<InstalledApp> appsList, List<AutoUploadSelects> selectsList);

  Observable<Boolean> refreshEvent();

  void loadPreviousAppsSelection(List<String> packageList);

  Single<List<InstalledApp>> getSelectedApps();

  Observable<Object> submitSelectionClick();

  Observable<List<AutoUploadSelects>> getAutoUploadSelectedApps(List<InstalledApp> packageList);

  public void clearSelection();
}
