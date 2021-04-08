package com.aptoide.uploader.apps.view;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.glide.GlideApp;
import com.aptoide.uploader.view.Rx.RxAlertDialog;
import com.aptoide.uploader.view.android.FragmentView;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SettingsFragment extends FragmentView implements SettingsView {
  private ImageView backButton;
  private TextView signOut;
  private ImageView profileAvatar;
  private TextView storeNameText;
  private LinearLayout autoUpload;
  private ImageView selectedApp1;
  private ImageView selectedApp2;
  private ImageView selectedApp3;
  private TextView selectedAppsExtra;
  private LinearLayout sendFeedback;
  private LinearLayout aboutUs;
  private LinearLayout termsConditions;
  private LinearLayout privacyPolicy;
  private RxAlertDialog logoutConfirmation;
  private List<ImageView> imageViewList;

  public SettingsFragment() {
  }

  public static SettingsFragment newInstance() {
    return new SettingsFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    backButton = view.findViewById(R.id.fragment_settings_back);
    profileAvatar = view.findViewById(R.id.fragment_settings_avatar);
    storeNameText = view.findViewById(R.id.fragment_settings_store_name);
    signOut = view.findViewById(R.id.fragment_settings_sign_out);
    autoUpload = view.findViewById(R.id.fragment_settings_autoupload);
    selectedApp1 = view.findViewById(R.id.fragment_settings_app_1);
    selectedApp2 = view.findViewById(R.id.fragment_settings_app_2);
    selectedApp3 = view.findViewById(R.id.fragment_settings_app_3);
    selectedAppsExtra = view.findViewById(R.id.fragment_settings_apps_extra);
    sendFeedback = view.findViewById(R.id.fragment_settings_feedback);
    aboutUs = view.findViewById(R.id.fragment_settings_aboutus);
    termsConditions = view.findViewById(R.id.fragment_settings_terms);
    privacyPolicy = view.findViewById(R.id.fragment_settings_privacy);

    logoutConfirmation = new RxAlertDialog.Builder(
        new ContextThemeWrapper(getContext(), R.style.ConfirmationDialog)).setMessage(
        R.string.logout_confirmation_message)
        .setPositiveButton(R.string.yes)
        .setNegativeButton(R.string.no)
        .build();

    new SettingsPresenter(new CompositeDisposable(), this, AndroidSchedulers.mainThread(),
        ((UploaderApplication) getContext().getApplicationContext()).getAutoLoginManager(),
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager(),
        ((UploaderApplication) getContext().getApplicationContext()).getAppsManager(),
        new SettingsNavigator(getFragmentManager(), getContext().getApplicationContext()),
        ((UploaderApplication) getContext().getApplicationContext()).getInstalledAppsManager()).present();
  }

  @Override public void onDestroyView() {
    backButton = null;
    //profileAvatar = null;
    //storeNameText = null;
    //selectedApp1 = null;
    //selectedApp2 = null;
    //selectedApp3 = null;
    //selectedAppsExtra = null;
    signOut = null;
    autoUpload = null;
    sendFeedback = null;
    aboutUs = null;
    termsConditions = null;
    privacyPolicy = null;
    //logoutConfirmation.dismiss();
    logoutConfirmation = null;
    GlideApp.get(getContext())
        .setMemoryCategory(MemoryCategory.NORMAL);
    super.onDestroyView();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    GlideApp.get(getContext())
        .setMemoryCategory(MemoryCategory.HIGH);
    return inflater.inflate(R.layout.fragment_settings, container, false);
  }

  @Override public void showAvatar(String avatarPath) {
    if (avatarPath != null && !avatarPath.trim()
        .isEmpty()) {
      Uri uri = Uri.parse(avatarPath);
      GlideApp.with(this)
          .load(uri)
          .apply(RequestOptions.circleCropTransform())
          .into(profileAvatar);
    } else {
      GlideApp.with(this)
          .load(getResources().getDrawable(R.drawable.avatar_default))
          .apply(RequestOptions.circleCropTransform())
          .into(profileAvatar);
    }
  }

  @Override public void showStoreName(@NotNull String storeName) {
    storeNameText.setText(storeName);
  }

  @Override public Observable<Object> signOutClick() {
    return RxView.clicks(signOut);
  }

  @Override public void showDialog() {
    if (!logoutConfirmation.isShowing()) {
      logoutConfirmation.show();
    }
  }

  @Override public void dismissDialog() {
    if (logoutConfirmation.isShowing()) {
      logoutConfirmation.dismiss();
    }
  }

  @Override public void showError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public Observable<DialogInterface> positiveClick() {
    return logoutConfirmation.positiveClicks();
  }

  @Override public void showSelectedApps(@NotNull List<InstalledApp> appsList) {
    int SELECTED_APPS_IMAGES_NUMBER = 3;
    if (appsList.size() <= SELECTED_APPS_IMAGES_NUMBER) {
      selectedAppsExtra.setVisibility(View.GONE);
    } else {
      int extraNumber = appsList.size() - SELECTED_APPS_IMAGES_NUMBER;
      String extraNumberString = "+" + extraNumber;
      selectedAppsExtra.setText(extraNumberString);
      selectedAppsExtra.setVisibility(View.VISIBLE);
    }
    addToImageViewList();
    setSelectedAppsImages(appsList, SELECTED_APPS_IMAGES_NUMBER);
  }

  private void addToImageViewList() {
    imageViewList = new ArrayList<>();
    imageViewList.add(selectedApp1);
    imageViewList.add(selectedApp2);
    imageViewList.add(selectedApp3);
  }

  private void setSelectedAppsImages(@NotNull List<InstalledApp> appsList, int imagesNumber) {
    int size = Math.min(appsList.size(), imagesNumber);
    for (int i = 0; i != size; i++) {
      GlideApp.with(this)
          .load(Uri.parse(appsList.get(i)
              .getIconPath()))
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(imageViewList.get(i));
      imageViewList.get(i)
          .setVisibility(View.VISIBLE);
    }
  }

  @Override public Observable<Object> backToMyStoreClick() {
    return RxView.clicks(backButton);
  }

  @Override public Observable<Object> autoUploadClick() {
    return RxView.clicks(autoUpload);
  }

  @Override public Observable<Object> sendFeedbackClick() {
    return RxView.clicks(sendFeedback);
  }

  @Override public Observable<Object> aboutUsClick() {
    return RxView.clicks(aboutUs);
  }

  @Override public Observable<Object> termsConditionsClick() {
    return RxView.clicks(termsConditions);
  }

  @Override public Observable<Object> privacyPolicyClick() {
    return RxView.clicks(privacyPolicy);
  }
}
