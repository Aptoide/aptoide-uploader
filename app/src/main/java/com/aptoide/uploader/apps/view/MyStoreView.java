package com.aptoide.uploader.apps.view;

import android.content.DialogInterface;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface MyStoreView extends View {

  void showApps(@NotNull List<InstalledApp> appsList);

  void showStoreName(@NotNull String storeName);

  void showDialog();

  void dismissDialog();

  Observable<DialogInterface> positiveClick();

  void showError();

  Observable<List<InstalledApp>> submitAppEvent();

  Observable<SortingOrder> orderByEvent();

  Observable<Object> logoutEvent();
}
