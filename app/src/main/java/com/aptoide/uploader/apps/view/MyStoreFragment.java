package com.aptoide.uploader.apps.view;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.permission.PermissionProvider;
import com.aptoide.uploader.apps.permission.UploadPermissionProvider;
import com.aptoide.uploader.view.Rx.RxAlertDialog;
import com.aptoide.uploader.view.android.FragmentView;
import com.jakewharton.rxbinding2.view.RxMenuItem;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MyStoreFragment extends FragmentView implements MyStoreView {

  private RecyclerView recyclerView;
  private MyAppsAdapter adapter;
  private TextView storeNameText;
  private Spinner spinner;
  private MenuItem logoutItem;
  private Toolbar toolbar;
  private RxAlertDialog logoutConfirmation;
  private View storeBanner;
  private View mainScreen;
  private Button submitButton;
  private Disposable selectionObservable;
  private Animation slideBottomDown;
  private Animation slideBottomUp;

  public static MyStoreFragment newInstance() {
    return new MyStoreFragment();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_my_apps, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    toolbar = view.findViewById(R.id.fragment_my_apps_toolbar);
    toolbar.inflateMenu(R.menu.app_grid_menu);
    logoutItem = toolbar.getMenu()
        .findItem(R.id.logout_button);

    SpannableString s = new SpannableString(getResources().getString(R.string.signout));
    s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
    logoutItem.setTitle(s);

    recyclerView = view.findViewById(R.id.fragment_my_apps_list);
    storeNameText = view.findViewById(R.id.fragment_my_apps_store_name);
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
    adapter = new MyAppsAdapter(new ArrayList<>());
    setUpSelectionListener();
    recyclerView.setAdapter(adapter);
    logoutConfirmation = new RxAlertDialog.Builder(
        new ContextThemeWrapper(getContext(), R.style.ConfirmationDialog)).setMessage(
        R.string.logout_confirmation_message)
        .setPositiveButton(R.string.yes)
        .setNegativeButton(R.string.no)
        .build();
    toolbar.setNavigationIcon(null);
    toolbar.setNavigationOnClickListener(click -> {
      adapter.clearAppsSelection();
    });
    storeBanner.setOnLongClickListener(click -> showVersionDialog());
    new MyStorePresenter(this,
        ((UploaderApplication) getContext().getApplicationContext()).getAppsManager(),
        new CompositeDisposable(), new MyStoreNavigator(getFragmentManager()),
        AndroidSchedulers.mainThread(),
        new UploadPermissionProvider((PermissionProvider) getContext()),
        ((UploaderApplication) getContext().getApplicationContext()).getAppUploadStatusPersistence(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploaderAnalytics()).present();
  }

  @Override public void onDestroyView() {
    adapter = null;
    spinner = null;
    storeNameText = null;
    recyclerView.setAdapter(null);
    recyclerView = null;
    selectionObservable.dispose();
    logoutConfirmation.dismiss();
    logoutConfirmation = null;
    logoutItem = null;
    toolbar = null;
    super.onDestroyView();
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
  }

  @Override public void orderApps(SortingOrder order) {
    adapter.setOrder(order);
  }

  @Override public void showStoreName(@NotNull String storeName) {
    storeNameText.setText(storeName);
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

  @Override public Observable<DialogInterface> positiveClick() {
    return logoutConfirmation.positiveClicks();
  }

  @Override public void showError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public Observable<Object> submitAppEvent() {
    return RxView.clicks(submitButton);
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

  @Override public Observable<Object> logoutEvent() {
    return RxMenuItem.clicks(logoutItem)
        .subscribeOn(AndroidSchedulers.mainThread())
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public Single<List<InstalledApp>> getSelectedApps() {
    return Single.just(adapter.getSelected());
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
      logoutItem.setVisible(false);
    } else {
      toolbar.setNavigationIcon(null);
      logoutItem.setVisible(true);
    }
  }

  @Override public void clearSelection() {
    adapter.clearAppsSelection();
  }

  @Override public void setCloudIcon(List<String> packageList) {
    if (packageList != null && adapter != null) {
      adapter.setCloudIcon(packageList);
    }
  }
}
