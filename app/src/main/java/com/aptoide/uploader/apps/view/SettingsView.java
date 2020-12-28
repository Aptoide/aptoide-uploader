package com.aptoide.uploader.apps.view;

import android.content.DialogInterface;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

public interface SettingsView extends View {
  public void showAvatar(String avatarPath);

  public void showStoreName(@NotNull String storeName);

  public Observable<Object> signOutClick();

  public void showDialog();

  public void dismissDialog();

  public void showError();

  public Observable<DialogInterface> positiveClick();

  public Observable<Object> backToMyStoreClick();

  public Observable<Object> autoUploadClick();

  public Observable<Object> sendFeedbackClick();

  public Observable<Object> aboutUsClick();

  public Observable<Object> termsConditionsClick();

  public Observable<Object> privacyPolicyClick();
}
