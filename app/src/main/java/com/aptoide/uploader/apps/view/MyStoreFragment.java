package com.aptoide.uploader.apps.view;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cm.aptoide.aptoideviews.recyclerview.GridRecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.AppUploadStatus;
import com.aptoide.uploader.apps.AutoUploadSelects;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.permission.PermissionProvider;
import com.aptoide.uploader.apps.permission.UploadPermissionProvider;
import com.aptoide.uploader.glide.GlideApp;
import com.aptoide.uploader.view.android.FragmentView;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MyStoreFragment extends FragmentView implements MyStoreView {

  private GridRecyclerView recyclerView;
  private ImageView featuretip_background;
  private TextView featuretip_rectangle;
  private MyAppsAdapter adapter;
  private TextView storeNameText;
  private ImageView profileAvatar;
  private Spinner spinner;
  private Button settingsItem;
  private Toolbar toolbar;
  private View storeBanner;
  private View mainScreen;
  private Button submitButton;
  private Disposable selectionObservable;
  private Animation slideBottomDown;
  private Animation slideBottomUp;
  private SwipeRefreshLayout refreshLayout;
  private PublishSubject<Boolean> refreshEvent;
  private ProgressBar loadingSpinner;
  private SortingOrder sortingOrder;

  public static MyStoreFragment newInstance() {
    return new MyStoreFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sortingOrder = SortingOrder.DATE;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    featuretip_background = view.findViewById(R.id.featuretip_background);
    featuretip_rectangle = view.findViewById(R.id.featuretip_rectangle);
    loadingSpinner = view.findViewById(R.id.loadingSPinner);
    toolbar = view.findViewById(R.id.fragment_my_apps_toolbar);
    toolbar.inflateMenu(R.menu.app_grid_menu);
    settingsItem = view.findViewById(R.id.settings_button);
    recyclerView = view.findViewById(R.id.fragment_my_apps_list);
    storeNameText = view.findViewById(R.id.fragment_my_apps_store_name);
    profileAvatar = view.findViewById(R.id.fragment_my_apps_profile_avatar);
    spinner = view.findViewById(R.id.sort_spinner);
    mainScreen = view.findViewById(R.id.grid_view_and_hint);
    storeBanner = view.findViewById(R.id.store_info);
    submitButton = view.findViewById(R.id.submit_button);
    setupEdgeToEdgeInsets(view);
    prepareSpinner(R.array.sort_spinner_array);
    setUpSubmitButtonAnimation();
    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
    recyclerView.addItemDecoration(new GridDividerItemDecoration(
        getResources().getDimensionPixelSize(R.dimen.apps_grid_item_margin)));
    recyclerView.setAdaptiveLayout(108, 152, GridRecyclerView.AdaptStrategy.SCALE_WIDTH_ONLY);
    MyStoreNavigator navigator = new MyStoreNavigator(getFragmentManager());
    adapter = new MyAppsAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
        (view1, packageName) -> {
          Uri packageURI = Uri.parse("package:" + packageName);
          Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
          startActivity(uninstallIntent);
        },
        (view1, packageName) -> navigator.navigateToAppDetails(packageName),
        sortingOrder);
    setUpSelectionListener();
    refreshEvent = PublishSubject.create();
    recyclerView.setAdapter(adapter);
    refreshLayout = view.findViewById(R.id.swipe_refresh);
    toolbar.setNavigationIcon(null);
    toolbar.setNavigationOnClickListener(click -> {
      adapter.clearAppsSelection();
    });
    storeBanner.setOnLongClickListener(click -> showVersionDialog());
    storeBanner.setOnClickListener(click -> navigateToStoreExternal(storeNameText.getText()
        .toString()));
    refreshLayout.setOnRefreshListener(() -> refreshEvent.onNext(true));

    new MyStorePresenter(this,
        ((UploaderApplication) getContext().getApplicationContext()).getAppsManager(),
        new CompositeDisposable(), new MyStoreNavigator(getFragmentManager()),
        AndroidSchedulers.mainThread(),
        new UploadPermissionProvider((PermissionProvider) getContext()),
        ((UploaderApplication) getContext().getApplicationContext()).getAppUploadStatusPersistence(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploaderAnalytics(),
        ((UploaderApplication) getContext().getApplicationContext()).getConnectivityProvider(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploadManager(),
        ((UploaderApplication) getContext().getApplicationContext()).getAutoLoginManager(),
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager(),
        ((UploaderApplication) getContext().getApplicationContext()).getInstalledAppsManager()).present();
  }

  @Override public void onDestroyView() {
    adapter = null;
    spinner = null;
    storeNameText = null;
    recyclerView.setAdapter(null);
    recyclerView = null;
    selectionObservable.dispose();
    settingsItem = null;
    toolbar = null;
    GlideApp.get(getContext())
        .setMemoryCategory(MemoryCategory.NORMAL);
    super.onDestroyView();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    GlideApp.get(getContext())
        .setMemoryCategory(MemoryCategory.HIGH);
    return inflater.inflate(R.layout.fragment_my_apps, container, false);
  }

  private void navigateToStoreExternal(String storeName) {
    if (storeName != null && !storeName.isEmpty()) {
      Intent sendIntent =
          new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.aptoide.com/store/" + storeName));
      sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(sendIntent);
    }
  }

  @Override public void checkFirstRun() {
    boolean isFirstRun = getContext().getSharedPreferences("PREFERENCE", 0)
        .getBoolean("isFirstRunTooltip", true);
    if (isFirstRun) {
      featuretip_background.setVisibility(View.VISIBLE);
      featuretip_rectangle.setVisibility(View.VISIBLE);
      featuretip_background.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          featuretip_background.setVisibility(View.GONE);
          featuretip_rectangle.setVisibility(View.GONE);
        }
      });
      getContext().getSharedPreferences("PREFERENCE", 0)
          .edit()
          .putBoolean("isFirstRunTooltip", false)
          .apply();
    }
  }

  @Override public void showApps(@NotNull List<InstalledApp> appsList,
      List<AppUploadStatus> appUploadStatuses, List<AutoUploadSelects> autoUploadSelects) {
    adapter.setInstalledAndUploadedApps(appsList, appUploadStatuses, autoUploadSelects);
    loadingSpinner.setVisibility(View.GONE);
    recyclerView.scheduleLayoutAnimation();
  }

  @Override public void refreshApps(@NotNull List<InstalledApp> appsList,
      List<AppUploadStatus> appUploadStatuses, List<AutoUploadSelects> autoUploadSelects) {
    adapter.setInstalledAndUploadedApps(appsList, appUploadStatuses, autoUploadSelects);
    refreshLayout.setRefreshing(false);
    recyclerView.scheduleLayoutAnimation();
  }

  @Override public void orderApps(SortingOrder order) {
    adapter.setOrder(order);
  }

  @Override public void showStoreName(@NotNull String storeName) {
    storeNameText.setText(storeName);
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

  @Override public void showError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showNoConnectivityError() {
    Toast.makeText(getContext(), R.string.no_connectivity_error, Toast.LENGTH_LONG)
        .show();
  }

  @Override public void showNotificationPermissionRequired() {
    Toast.makeText(getContext(), R.string.notification_permission_required, Toast.LENGTH_LONG)
        .show();
  }

  @Override public Observable<Object> submitAppEvent() {
    return RxView.clicks(submitButton)
        .map(o -> "");
  }

  @Override public Observable<SortingOrder> orderByEvent() {
    return RxAdapterView.itemSelections(spinner)
        .map(integer -> {
          if (integer == 0) {
            return SortingOrder.DATE;
          } else {
            return SortingOrder.NAME;
          }
        });
  }

  @Override public void setSubmitButtonVisibility(boolean appsSelected) {
    if (appsSelected) {
      TranslateAnimation translateAnimation =
          new TranslateAnimation(0, 0, 0, -storeBanner.getHeight());
      translateAnimation.setDuration(200);
      translateAnimation.setFillAfter(true);

      translateAnimation.setAnimationListener(new Animation.AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {
        }

        @Override public void onAnimationEnd(Animation animation) {
          mainScreen.setTranslationY(-storeBanner.getHeight());
          mainScreen.clearAnimation();
        }

        @Override public void onAnimationRepeat(Animation animation) {

        }
      });

      mainScreen.startAnimation(translateAnimation);
      submitButton.startAnimation(slideBottomUp);
    } else {
      TranslateAnimation translateAnimation =
          new TranslateAnimation(0, 0, 0, storeBanner.getHeight());
      translateAnimation.setDuration(200);
      translateAnimation.setFillAfter(true);

      translateAnimation.setAnimationListener(new Animation.AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {
        }

        @Override public void onAnimationEnd(Animation animation) {
          mainScreen.setTranslationY(0);
          mainScreen.clearAnimation();
        }

        @Override public void onAnimationRepeat(Animation animation) {

        }
      });

      mainScreen.startAnimation(translateAnimation);
      submitButton.startAnimation(slideBottomDown);
    }
  }

  @Override public Observable<Object> goToSettings() {
    return RxView.clicks(settingsItem)
        .subscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public Single<List<InstalledApp>> getSelectedApps() {
    return Single.just(adapter.getSelected());
  }

  @Override public void clearSelection() {
    adapter.clearAppsSelection();
  }

  @Override public Observable<Boolean> refreshEvent() {
    return refreshEvent;
  }

  private boolean showVersionDialog() {
    PackageInfo pInfo;
    try {
      pInfo = getActivity().getPackageManager()
          .getPackageInfo(getActivity().getPackageName(), 0);
      String version = pInfo.versionName;
      int versionCode = pInfo.versionCode;
      String appName = pInfo.packageName;

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setMessage("App : "
          + appName
          + "\n"
          + "Version : "
          + version
          + "\n"
          + "Version Code : "
          + versionCode);

      AlertDialog dialog = builder.create();
      dialog.show();
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return false;
  }

  private void setUpSelectionListener() {
    selectionObservable = adapter.toggleSelection()
        .doOnNext(appsSelected -> handleTitleChange())
        .distinctUntilChanged()
        .doOnNext(appsSelected -> setSubmitButtonVisibility(appsSelected))
        .subscribe();
  }

  private void prepareSpinner(int arrayId) {
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getActivity(), arrayId, R.layout.spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  public void setUpSubmitButtonAnimation() {

    final Animation.AnimationListener showBottom = new Animation.AnimationListener() {
      @Override public void onAnimationStart(Animation animation) {
        submitButton.setVisibility(View.VISIBLE);
      }

      @Override public void onAnimationEnd(Animation animation) {

      }

      @Override public void onAnimationRepeat(Animation animation) {

      }
    };

    final Animation.AnimationListener hideBottom = new Animation.AnimationListener() {
      @Override public void onAnimationStart(Animation animation) {
        submitButton.setVisibility(View.GONE);
      }

      @Override public void onAnimationEnd(Animation animation) {

      }

      @Override public void onAnimationRepeat(Animation animation) {

      }
    };

    slideBottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_bottom_down);
    slideBottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_bottom_up);
    slideBottomUp.setAnimationListener(showBottom);
    slideBottomDown.setAnimationListener(hideBottom);
  }

  public void handleTitleChange() {
    int selected = adapter.getSelectedCount();

    if (selected != 0) {
      setToolbarVisibility(true);
      if (selected == 1) {
        toolbar.setTitle(
            String.valueOf(adapter.getSelectedCount()) + " " + getResources().getString(
                R.string.app_selected));
      } else {
        toolbar.setTitle(
            String.valueOf(adapter.getSelectedCount()) + " " + getResources().getString(
                R.string.apps_selected));
      }
    } else {
      setToolbarVisibility(false);
      toolbar.setTitle(R.string.app_name);
    }
  }

  private void setToolbarVisibility(boolean shouldShow) {
    if (shouldShow) {
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
      settingsItem.setVisibility(View.GONE);
    } else {
      toolbar.setNavigationIcon(null);
      settingsItem.setVisibility(View.VISIBLE);
    }
  }

  private void setupEdgeToEdgeInsets(View view) {
    // Handle top insets for toolbar (status bar) - toolbar extends behind status bar
    ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
      return insets;
    });

    // Handle top insets for main content - needs to account for status bar + toolbar
    ViewCompat.setOnApplyWindowInsetsListener(mainScreen, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
      // Get actionBarSize in pixels
      int actionBarSize = 0;
      android.util.TypedValue tv = new android.util.TypedValue();
      if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        actionBarSize = android.util.TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
      }
      params.topMargin = systemBars.top + actionBarSize;
      v.setLayoutParams(params);
      return insets;
    });

    // Handle top insets for settings button
    ViewCompat.setOnApplyWindowInsetsListener(settingsItem, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
      int dpMargin = (int) (10 * getResources().getDisplayMetrics().density);
      params.topMargin = systemBars.top + dpMargin;
      v.setLayoutParams(params);
      return insets;
    });

    // Handle top insets for feature tip rectangle
    ViewCompat.setOnApplyWindowInsetsListener(featuretip_rectangle, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
      int dpMargin = (int) (60 * getResources().getDisplayMetrics().density);
      params.topMargin = systemBars.top + dpMargin;
      v.setLayoutParams(params);
      return insets;
    });

    // Handle bottom insets for submit button
    ViewCompat.setOnApplyWindowInsetsListener(submitButton, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
      params.bottomMargin = systemBars.bottom;
      v.setLayoutParams(params);
      return insets;
    });

    // Handle bottom insets for RecyclerView to ensure last row is visible
    ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
      return insets;
    });
    recyclerView.setClipToPadding(false);
  }
}
