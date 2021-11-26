package com.aptoide.uploader.apps.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.AutoUploadSelects;
import com.aptoide.uploader.apps.InstalledApp;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoUploadAppsAdapter extends RecyclerView.Adapter<AutoUploadAppViewHolder> {

  private final List<InstalledApp> installedApps;
  private final List<AutoUploadSelects> autoUploadAppsList;
  private final List<Integer> selectedApps;
  private final AppSelectedListener selectedAppListener;
  private final PublishSubject<Boolean> selectedPublisher;
  private final List<Integer> initialSelectedApps;
  private SortingOrder currentOrder;

  public AutoUploadAppsAdapter(@NonNull List<InstalledApp> list,
      List<AutoUploadSelects> autoUploadAppsList, SortingOrder currentOrder) {
    this.installedApps = list;
    this.autoUploadAppsList = autoUploadAppsList;
    this.currentOrder = currentOrder;
    this.selectedApps = new ArrayList<>();
    this.selectedAppListener = (view, position) -> selectApp(position);
    this.selectedPublisher = PublishSubject.create();
    this.initialSelectedApps = new ArrayList<>();
  }

  @Override public AutoUploadAppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AutoUploadAppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app_auto_upload, parent, false), selectedAppListener);
  }

  @Override public void onBindViewHolder(AutoUploadAppViewHolder holder, int position) {
    holder.setApp(installedApps.get(position), selectedApps.contains(position));
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getItemCount() {
    return installedApps.size();
  }

  public Observable<Boolean> toggleSelection() {
    return selectedPublisher;
  }

  public void loadPreviousAppsSelection(List<String> selectedPackageNames) {
    for (int i = 0; i < installedApps.size(); i++) {
      InstalledApp app = installedApps.get(i);
      for (String packageName : selectedPackageNames) {
        if (app.getPackageName()
            .equals(packageName)) {
          initialSelectedApps.add(i);
          selectApp(i);
        }
      }
    }
  }

  public void setInstalledAndSelectedApps(List<InstalledApp> appsList,
      List<AutoUploadSelects> selectedList) {
    setInstalledApps(appsList);
    setAutoUploadSelectsApps(selectedList);
  }

  public void setInstalledApps(List<InstalledApp> appsList) {
    if (!appsList.equals(installedApps)) {
      installedApps.clear();
      installedApps.addAll(appsList);
      clearAppsSelection(false);
      setOrder(currentOrder);
      notifyDataSetChanged();
    }
  }

  public void setAutoUploadSelectsApps(List<AutoUploadSelects> selectsList) {
    if (!selectsList.equals(autoUploadAppsList)) {
      autoUploadAppsList.clear();
      autoUploadAppsList.addAll(selectsList);
      clearAppsSelection(false);
      setOrder(currentOrder);
      notifyDataSetChanged();
    }
  }

  public void setOrder(SortingOrder order) {
    currentOrder = order;
    if (order.equals(SortingOrder.DATE)) {
      Collections.sort(installedApps,
          (obj1, obj2) -> Long.compare(obj2.getInstalledDate(), obj1.getInstalledDate()));
    }
    selectedApps.clear();
  }

  public List<InstalledApp> getSelectedApps() {
    List<InstalledApp> selectedAppsList = new ArrayList<>();
    for (Integer appId : selectedApps) {
      selectedAppsList.add(installedApps.get(appId));
    }
    return selectedAppsList;
  }

  public void selectApp(int position) {
    if (selectedApps.contains(position)) {
      selectedApps.remove((Integer) position);
    } else {
      selectedApps.add(position);
    }
    selectedPublisher.onNext(hasListChanged(selectedApps, initialSelectedApps));
    notifyItemChanged(position);
  }

  private boolean hasListChanged(List<Integer> selectedApps, List<Integer> initialSelectedApps) {
    if (selectedApps.size() != initialSelectedApps.size()) {
      return true;
    } else {
      for (Integer selectedApp : selectedApps) {
        if (!initialSelectedApps.contains(selectedApp)) {
          return true;
        }
      }
    }
    return false;
  }

  private void clearAppsSelection(boolean notify) {
    selectedApps.clear();
    selectedPublisher.onNext(false);
    if (notify) {
      notifyDataSetChanged();
    }
  }

  public List<AutoUploadSelects> setSelectedApps(List<InstalledApp> selectedApps) {
    for (int i = 0; i < autoUploadAppsList.size(); i++) {
      boolean setted = false;
      for (InstalledApp selectedApp : selectedApps) {
        if (autoUploadAppsList.get(i)
            .getPackageName()
            .equals(selectedApp.getPackageName())) {
          autoUploadAppsList.get(i)
              .setSelectedAutoUpload(true);
          setted = true;
        }
      }
      if (!setted) {
        autoUploadAppsList.get(i)
            .setSelectedAutoUpload(false);
      }
    }
    return autoUploadAppsList;
  }

  public void clearAppsSelection() {
    clearAppsSelection(true);
  }
}