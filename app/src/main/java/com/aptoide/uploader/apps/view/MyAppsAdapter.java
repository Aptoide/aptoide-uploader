package com.aptoide.uploader.apps.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.AppUploadStatus;
import com.aptoide.uploader.apps.AutoUploadSelects;
import com.aptoide.uploader.apps.InstalledApp;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

  private final List<InstalledApp> installedApps;
  private final List<AppUploadStatus> uploadedList;
  private final List<Integer> selectedApps;
  private final List<AutoUploadSelects> autoUploadSelectsList;
  private final AppSelectedListener selectedAppListener;
  private final AppLongClickListener longClickListener;
  private final PublishSubject<Boolean> selectedPublisher;
  private SortingOrder currentOrder;

  public MyAppsAdapter(@NonNull List<InstalledApp> list, List<AppUploadStatus> uploadedList,
      List<AutoUploadSelects> autoUploadSelectsList, AppLongClickListener longClickListener,
      SortingOrder currentOrder) {
    this.installedApps = list;
    this.uploadedList = uploadedList;
    this.autoUploadSelectsList = autoUploadSelectsList;
    this.longClickListener = longClickListener;
    this.currentOrder = currentOrder;
    this.selectedApps = new ArrayList<>();
    this.selectedAppListener = (view, position) -> setSelected(position);
    this.selectedPublisher = PublishSubject.create();
  }

  @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app, parent, false), selectedAppListener, longClickListener);
  }

  @Override public void onBindViewHolder(AppViewHolder holder, int position) {
    holder.setApp(installedApps.get(position), selectedApps.contains(position),
        matchInstalledToUploadStatus(installedApps.get(position)),
        isAppOnAutoUpload(installedApps.get(position)));
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getItemCount() {
    return installedApps.size();
  }

  private boolean isAppOnAutoUpload(InstalledApp installedApp) {
    for (AutoUploadSelects autoUploadSelect : autoUploadSelectsList) {
      if (autoUploadSelect.getPackageName()
          .equals(installedApp.getPackageName()) && autoUploadSelect.isSelectedAutoUpload()) {
        return true;
      }
    }
    return false;
  }

  public Observable<Boolean> toggleSelection() {
    return selectedPublisher;
  }

  public void setInstalledAndUploadedApps(List<InstalledApp> appsList,
      List<AppUploadStatus> appUploadStatuses, List<AutoUploadSelects> autoUploadSelects) {
    setInstalledApps(appsList);
    setUploadStatusApps(appUploadStatuses);
    setAutoUploadApps(autoUploadSelects);
  }

  private void setAutoUploadApps(List<AutoUploadSelects> autoUploadSelects) {
    if (!autoUploadSelects.equals(this.autoUploadSelectsList)) {
      this.autoUploadSelectsList.clear();
      this.autoUploadSelectsList.addAll(autoUploadSelects);
      clearAppsSelection(false);
      setOrder(currentOrder);
      notifyDataSetChanged();
    }
  }

  public void setInstalledApps(List<InstalledApp> appsList) {
    if (!appsList.equals(installedApps)) {
      installedApps.clear();
      installedApps.addAll(appsList);
      clearAppsSelection(false);
      setOrder(currentOrder);
    }
  }

  public void setUploadStatusApps(List<AppUploadStatus> appUploadStatuses) {
    if (!appUploadStatuses.equals(uploadedList)) {
      uploadedList.clear();
      uploadedList.addAll(appUploadStatuses);
      clearAppsSelection(false);
      setOrder(currentOrder);
      notifyDataSetChanged();
    }
  }

  public AppUploadStatus matchInstalledToUploadStatus(InstalledApp installedApp) {
    AppUploadStatus appUploadStatus = null;
    for (AppUploadStatus appStatus : uploadedList) {
      if (appStatus.getPackageName()
          .equals(installedApp.getPackageName())) {
        appUploadStatus = appStatus;
      }
    }
    return appUploadStatus;
  }

  public void setOrder(SortingOrder order) {
    currentOrder = order;
    if (order.equals(SortingOrder.NAME)) {
      Collections.sort(installedApps, (obj1, obj2) -> obj1.getName()
          .compareToIgnoreCase(obj2.getName()));
    }
    if (order.equals(SortingOrder.DATE)) {
      Collections.sort(installedApps,
          (obj1, obj2) -> Long.compare(obj2.getInstalledDate(), obj1.getInstalledDate()));
    }
    selectedApps.clear();
    notifyDataSetChanged();
  }

  private void clearAppsSelection(boolean notify) {
    selectedApps.clear();
    selectedPublisher.onNext(false);
    if (notify) {
      notifyDataSetChanged();
    }
  }

  public int getSelectedCount() {
    return selectedApps.size();
  }

  public List<InstalledApp> getSelected() {
    List<InstalledApp> selectedAppsList = new ArrayList<>();
    for (Integer appId : selectedApps) {
      selectedAppsList.add(installedApps.get(appId));
    }
    return selectedAppsList;
  }

  public void setSelected(int position) {
    if (selectedApps.contains(position)) {
      selectedApps.remove((Integer) position);
      if (selectedApps.size() == 0) {
        selectedPublisher.onNext(false);
      } else {
        selectedPublisher.onNext(true);
      }
    } else {
      selectedApps.add(position);
      selectedPublisher.onNext(true);
    }
    notifyItemChanged(position);
  }

  public void clearAppsSelection() {
    clearAppsSelection(true);
  }
}
