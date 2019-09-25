package com.aptoide.uploader.apps.view;

import android.content.DialogInterface;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface MyStoreView extends View {

  void showApps(@NotNull List<InstalledApp> appsList);

  void orderApps(SortingOrder order);

  void showStoreName(@NotNull String storeName);

  void showDialog();

  void dismissDialog();

  Observable<DialogInterface> positiveClick();

  void showError();

  void showNoConnectivityError();

  Observable<Object> submitAppEvent();

  Observable<SortingOrder> orderByEvent();

  void setSubmitButtonVisibility(boolean status);

  Observable<Object> logoutEvent();

  Single<List<InstalledApp>> getSelectedApps();

  void clearSelection();

  void setCloudIcon(List<String> md5List);
}
