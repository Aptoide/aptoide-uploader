package com.aptoide.uploader.apps.view;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.AppDetailsDataProvider;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.glide.GlideApp;
import com.aptoide.uploader.view.android.FragmentView;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.io.File;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AppDetailsFragment extends FragmentView implements AppDetailsView {

  private static final String ARG_PACKAGE_NAME = "package_name";

  private ImageView backButton;
  private ImageView appIcon;
  private TextView appName;
  private TextView packageNameHeader;
  private TextView versionName;
  private TextView versionCode;
  private TextView targetSdk;
  private TextView minSdk;
  private TextView packageName;
  private TextView appPath;
  private TextView appSize;
  private TextView appType;
  private LinearLayout filesContainer;
  private TextView installDate;
  private TextView updateDate;
  private ProgressBar loadingSpinner;
  private Toolbar toolbar;
  private ScrollView scrollView;

  public AppDetailsFragment() {
  }

  public static AppDetailsFragment newInstance(String packageName) {
    AppDetailsFragment fragment = new AppDetailsFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PACKAGE_NAME, packageName);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_app_details, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    toolbar = view.findViewById(R.id.fragment_app_details_toolbar);
    scrollView = view.findViewById(R.id.fragment_app_details_scroll);
    backButton = view.findViewById(R.id.fragment_app_details_back);
    appIcon = view.findViewById(R.id.fragment_app_details_icon);
    appName = view.findViewById(R.id.fragment_app_details_app_name);
    packageNameHeader = view.findViewById(R.id.fragment_app_details_package_name_header);
    versionName = view.findViewById(R.id.fragment_app_details_version_name);
    versionCode = view.findViewById(R.id.fragment_app_details_version_code);
    targetSdk = view.findViewById(R.id.fragment_app_details_target_sdk);
    minSdk = view.findViewById(R.id.fragment_app_details_min_sdk);
    packageName = view.findViewById(R.id.fragment_app_details_package_name);
    appPath = view.findViewById(R.id.fragment_app_details_app_path);
    appSize = view.findViewById(R.id.fragment_app_details_app_size);
    appType = view.findViewById(R.id.fragment_app_details_app_type);
    filesContainer = view.findViewById(R.id.fragment_app_details_files_container);
    installDate = view.findViewById(R.id.fragment_app_details_install_date);
    updateDate = view.findViewById(R.id.fragment_app_details_update_date);
    loadingSpinner = view.findViewById(R.id.fragment_app_details_loading);

    setupEdgeToEdgeInsets(view);

    String pkgName = getArguments() != null ? getArguments().getString(ARG_PACKAGE_NAME) : null;

    if (pkgName != null) {
      new AppDetailsPresenter(this,
          ((UploaderApplication) getContext().getApplicationContext()).getInstalledAppsProvider(),
          new AppDetailsDataProvider(),
          new AppDetailsNavigator(getFragmentManager()),
          new CompositeDisposable(),
          AndroidSchedulers.mainThread(),
          pkgName).present();
    }
  }

  @Override public void onDestroyView() {
    backButton = null;
    appIcon = null;
    appName = null;
    packageNameHeader = null;
    versionName = null;
    versionCode = null;
    targetSdk = null;
    minSdk = null;
    packageName = null;
    appPath = null;
    appSize = null;
    appType = null;
    filesContainer = null;
    installDate = null;
    updateDate = null;
    loadingSpinner = null;
    toolbar = null;
    scrollView = null;
    super.onDestroyView();
  }

  @Override public void showAppDetails(@NotNull InstalledApp app) {
    appName.setText(app.getName());
    packageNameHeader.setText(app.getPackageName());
    versionName.setText(app.getVersionName() != null ? app.getVersionName() : "N/A");
    versionCode.setText(String.valueOf(app.getVersionCode()));
    targetSdk.setText(String.valueOf(app.getTargetSdkVersion()));
    minSdk.setText(String.valueOf(app.getMinSdkVersion()));
    packageName.setText(app.getPackageName());
    appPath.setText(app.getApkPath());
  }

  @Override public void showAppIcon(String iconPath) {
    if (iconPath != null && !iconPath.isEmpty()) {
      GlideApp.with(this)
          .load(Uri.parse(iconPath))
          .transform(new RoundedCorners(16))
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(appIcon);
    }
  }

  @Override public void showAppSize(String formattedSize) {
    appSize.setText(formattedSize);
  }

  @Override public void showAppType(boolean isAppBundle, int splitCount) {
    if (isAppBundle) {
      String typeText = getString(R.string.app_type_bundle) + " (" + splitCount + " splits)";
      appType.setText(typeText);
    } else {
      appType.setText(R.string.app_type_universal);
    }
  }

  @Override public void showFilePaths(List<String> paths) {
    filesContainer.removeAllViews();
    
    for (int i = 0; i < paths.size(); i++) {
      String path = paths.get(i);
      String fileName = new File(path).getName();
      
      TextView pathView = new TextView(getContext());
      pathView.setTextColor(getResources().getColor(R.color.black));
      pathView.setTextSize(11);
      pathView.setTextIsSelectable(true);
      
      // Format: filename (if split) or just the path for base APK
      if (i == 0) {
        pathView.setText("Base: " + fileName);
      } else {
        pathView.setText("Split " + i + ": " + fileName);
      }
      
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 4, 0, 4);
      pathView.setLayoutParams(params);
      
      filesContainer.addView(pathView);
    }
  }

  @Override public void showInstallDate(String formattedDate) {
    installDate.setText(formattedDate);
  }

  @Override public void showUpdateDate(String formattedDate) {
    updateDate.setText(formattedDate);
  }

  @Override public void showError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
  }

  @Override public void showLoading() {
    loadingSpinner.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    loadingSpinner.setVisibility(View.GONE);
  }

  @Override public Observable<Object> backButtonClick() {
    return RxView.clicks(backButton);
  }

  private void setupEdgeToEdgeInsets(View view) {
    // Handle top insets for toolbar (status bar)
    ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
      return insets;
    });

    // Handle bottom insets for scroll view content
    ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
      return insets;
    });
    scrollView.setClipToPadding(false);
  }
}
