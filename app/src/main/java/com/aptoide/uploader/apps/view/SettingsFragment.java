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
import com.aptoide.uploader.view.Rx.RxAlertDialog;
import com.aptoide.uploader.view.android.FragmentView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;

public class SettingsFragment extends FragmentView implements SettingsView {
  private ImageView backButton;
  private TextView signOut;
  private ImageView profileAvatar;
  private TextView storeNameText;
  private LinearLayout autoUpload;
  private LinearLayout sendFeedback;
  private LinearLayout aboutUs;
  private LinearLayout termsConditions;
  private LinearLayout privacyPolicy;
  private RxAlertDialog logoutConfirmation;

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
        new SettingsNavigator(getFragmentManager(),
            getContext().getApplicationContext())).present();
  }

  @Override public void onDestroyView() {
    backButton = null;
    profileAvatar = null;
    storeNameText = null;
    signOut = null;
    autoUpload = null;
    sendFeedback = null;
    aboutUs = null;
    termsConditions = null;
    privacyPolicy = null;
    logoutConfirmation.dismiss();
    logoutConfirmation = null;
    super.onDestroyView();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_settings, container, false);
  }

  @Override public void showAvatar(String avatarPath) {
    if (avatarPath != null && !avatarPath.trim()
        .isEmpty()) {
      Uri uri = Uri.parse(avatarPath);
      Glide.with(this)
          .load(uri)
          .apply(RequestOptions.circleCropTransform())
          .into(profileAvatar);
    } else {
      Glide.with(this)
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
