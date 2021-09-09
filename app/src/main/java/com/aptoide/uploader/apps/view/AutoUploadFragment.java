package com.aptoide.uploader.apps.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.AutoUploadSelects;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.permission.PermissionProvider;
import com.aptoide.uploader.apps.permission.UploadPermissionProvider;
import com.aptoide.uploader.view.android.FragmentView;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AutoUploadFragment extends FragmentView implements AutoUploadView {
  private Toolbar toolbar;
  private ImageView backButton;
  private RecyclerView recyclerView;
  private SwipeRefreshLayout refreshLayout;
  private AutoUploadAppsAdapter adapter;
  private PublishSubject<Boolean> refreshEvent;
  private Button submitButton;
  private Disposable selectionObservable;
  private Animation slideBottomDown;
  private Animation slideBottomUp;

  public AutoUploadFragment() {
  }

  public static AutoUploadFragment newInstance() {
    return new AutoUploadFragment();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    toolbar = view.findViewById(R.id.fragment_autoupload_toolbar);
    backButton = view.findViewById(R.id.fragment_autoupload_back);
    submitButton = view.findViewById(R.id.submit_autoupload_button);
    recyclerView = view.findViewById(R.id.fragment_autoupload_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.addItemDecoration(
        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    adapter = new AutoUploadAppsAdapter(new ArrayList<>(), new ArrayList<>(), SortingOrder.DATE);
    recyclerView.setAdapter(adapter);
    refreshLayout = view.findViewById(R.id.fragment_autoupload_swipe_refresh);
    refreshEvent = PublishSubject.create();
    refreshLayout.setOnRefreshListener(() -> refreshEvent.onNext(true));
    setUpSubmitButtonAnimation();
    setUpSelectionListener();
    toolbar.setNavigationOnClickListener(click -> adapter.clearAppsSelection());

    new AutoUploadPresenter(this, new CompositeDisposable(), AndroidSchedulers.mainThread(),
        new AutoUploadNavigator(getFragmentManager()),
        new UploadPermissionProvider((PermissionProvider) getContext()),
        ((UploaderApplication) getContext().getApplicationContext()).getInstalledAppsManager()).present();
  }

  @Override public void onDestroyView() {
    toolbar = null;
    backButton = null;
    submitButton = null;
    refreshLayout = null;
    refreshEvent = null;
    selectionObservable.dispose();
    super.onDestroyView();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_auto_upload, container, false);
  }

  @Override public void showError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public Observable<Object> backToSettingsClick() {
    return RxView.clicks(backButton);
  }

  @Override
  public void showApps(@NotNull List<InstalledApp> appsList, List<AutoUploadSelects> selectedList) {
    adapter.setInstalledAndSelectedApps(appsList, selectedList);
    recyclerView.scheduleLayoutAnimation();
    recyclerView.setVisibility(View.VISIBLE);
  }

  @Override public void refreshApps(@NotNull List<InstalledApp> appsList,
      List<AutoUploadSelects> selectedList) {
    adapter.setInstalledAndSelectedApps(appsList, selectedList);
    refreshLayout.setRefreshing(false);
    recyclerView.scheduleLayoutAnimation();
  }

  @Override public Observable<Boolean> refreshEvent() {
    return refreshEvent;
  }

  @Override public void loadPreviousAppsSelection(List<String> packageList) {
    adapter.loadPreviousAppsSelection(packageList);
  }

  @Override public Single<List<InstalledApp>> getSelectedApps() {
    return Single.just(adapter.getSelectedApps());
  }

  @Override public Observable<Object> submitSelectionClick() {
    return RxView.clicks(submitButton);
  }

  @Override public Observable<List<AutoUploadSelects>> getAutoUploadSelectedApps(
      List<InstalledApp> packageList) {
    return Observable.just(adapter.setSelectedApps(packageList));
  }

  @Override public void clearSelection() {
    adapter.clearAppsSelection();
  }

  public void setSubmitButtonVisibility(boolean appsSelected) {
    if (appsSelected) {
      submitButton.startAnimation(slideBottomUp);
    } else {
      submitButton.startAnimation(slideBottomDown);
    }
  }

  private void setUpSelectionListener() {
    selectionObservable = adapter.toggleSelection()
        .distinctUntilChanged()
        .doOnNext(appsSelected -> setSubmitButtonVisibility(appsSelected))
        .subscribe();
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
}
