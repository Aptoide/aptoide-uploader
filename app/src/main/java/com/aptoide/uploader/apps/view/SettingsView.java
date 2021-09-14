package com.aptoide.uploader.apps.view;

import android.content.DialogInterface;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface SettingsView extends View {
  void showAvatar(String avatarPath);

  void showStoreName(@NotNull String storeName);

  Observable<Object> signOutClick();

  void showDialog();

  void dismissDialog();

  void showError();

  Observable<DialogInterface> positiveClick();

  void showSelectedApps(@NotNull List<InstalledApp> appsList);

  Observable<Object> backToMyStoreClick();

  Observable<Object> autoUploadClick();

  Observable<Object> sendFeedbackClick();

  Observable<Object> aboutUsClick();

  Observable<Object> termsConditionsClick();

  Observable<Object> privacyPolicyClick();
}
