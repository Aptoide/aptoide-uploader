package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.AppUploadStatus;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface MyStoreView extends View {

  void checkFirstRun();

  void showApps(@NotNull List<InstalledApp> appsList, List<AppUploadStatus> appUploadStatuses);

  void refreshApps(@NotNull List<InstalledApp> appsList, List<AppUploadStatus> appUploadStatuses);

  void orderApps(SortingOrder order);

  void showStoreName(@NotNull String storeName);

  void showAvatar(String avatarPath);

  void showError();

  void showNoConnectivityError();

  Observable<Object> submitAppEvent();

  Observable<SortingOrder> orderByEvent();

  void setSubmitButtonVisibility(boolean status);

  Observable<Object> goToSettings();

  Single<List<InstalledApp>> getSelectedApps();

  void clearSelection();

  Observable<Boolean> refreshEvent();
}
