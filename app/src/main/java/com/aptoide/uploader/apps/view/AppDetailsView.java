package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import java.util.List;

public interface AppDetailsView extends View {

  void showAppDetails(InstalledApp app);

  void showAppIcon(String iconPath);

  void showAppSize(String formattedSize);

  void showAppType(boolean isAppBundle, int splitCount);

  void showFilePaths(List<String> paths);

  void showInstallDate(String formattedDate);

  void showUpdateDate(String formattedDate);

  void showError();

  void showLoading();

  void hideLoading();

  Observable<Object> backButtonClick();
}
