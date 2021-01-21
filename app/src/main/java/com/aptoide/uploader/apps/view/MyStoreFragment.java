package com.aptoide.uploader.apps.view;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cm.aptoide.aptoideviews.recyclerview.GridRecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.permission.PermissionProvider;
import com.aptoide.uploader.apps.permission.UploadPermissionProvider;
import com.aptoide.uploader.view.android.FragmentView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxMenuItem;
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
  private MyAppsAdapter adapter;
  private TextView storeNameText;
  private ImageView profileAvatar;
  private Spinner spinner;
  private MenuItem settingsItem;
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
    loadingSpinner = view.findViewById(R.id.loadingSPinner);
    toolbar = view.findViewById(R.id.fragment_my_apps_toolbar);
    toolbar.inflateMenu(R.menu.app_grid_menu);
    settingsItem = toolbar.getMenu()
        .findItem(R.id.settings_button);

    recyclerView = view.findViewById(R.id.fragment_my_apps_list);
    storeNameText = view.findViewById(R.id.fragment_my_apps_store_name);
    profileAvatar = view.findViewById(R.id.fragment_my_apps_profile_avatar);
    spinner = view.findViewById(R.id.sort_spinner);
    mainScreen = view.findViewById(R.id.grid_view_and_hint);
    storeBanner = view.findViewById(R.id.store_info);
    submitButton = view.findViewById(R.id.submit_button);
    toolbar = view.findViewById(R.id.fragment_my_apps_toolbar);
    prepareSpinner(R.array.sort_spinner_array);
    setUpSubmitButtonAnimation();
    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
    recyclerView.addItemDecoration(new GridDividerItemDecoration(
        getResources().getDimensionPixelSize(R.dimen.apps_grid_item_margin)));
    recyclerView.setAdaptiveLayout(110, 126,
        GridRecyclerView.AdaptStrategy.SCALE_KEEP_ASPECT_RATIO);
    adapter = new MyAppsAdapter(new ArrayList<>(), (view1, packageName) -> {
      Uri packageURI = Uri.parse("package:" + packageName);
      Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
      startActivity(uninstallIntent);
    }, sortingOrder);
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
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager()).present();
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
    Glide.get(getContext())
        .setMemoryCategory(MemoryCategory.NORMAL);
    super.onDestroyView();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    Glide.get(getContext())
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

  @Override public void showApps(@NotNull List<InstalledApp> appsList) {
    adapter.setInstalledApps(appsList);
    loadingSpinner.setVisibility(View.GONE);
    recyclerView.scheduleLayoutAnimation();
  }

  @Override public void refreshApps(@NotNull List<InstalledApp> appsList) {
    adapter.refreshInstalledApps(appsList);
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

  @Override public void showError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showNoConnectivityError() {
    Toast.makeText(getContext(), R.string.no_connectivity_error, Toast.LENGTH_LONG)
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
    return RxMenuItem.clicks(settingsItem)
        .subscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public Single<List<InstalledApp>> getSelectedApps() {
    return Single.just(adapter.getSelected());
  }

  @Override public void clearSelection() {
    adapter.clearAppsSelection();
  }

  @Override public void setCloudIcon(List<String> packageList) {
    if (packageList != null && adapter != null) {
      adapter.setCloudIcon(packageList);
    }
  }

  @Override public Observable<Boolean> refreshEvent() {
    return refreshEvent;
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
      settingsItem.setVisible(false);
    } else {
      toolbar.setNavigationIcon(null);
      settingsItem.setVisible(true);
    }
  }
}
