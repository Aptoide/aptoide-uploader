package com.aptoide.uploader.apps.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoUploadAppsAdapter extends RecyclerView.Adapter<AutoUploadAppViewHolder> {

  private final List<InstalledApp> installedApps;
  private final List<Integer> selectedApps;
  private final AppSelectedListener selectedAppListener;
  private final PublishSubject<Boolean> selectedPublisher;
  private SortingOrder currentOrder;

  public AutoUploadAppsAdapter(@NonNull List<InstalledApp> list, SortingOrder currentOrder) {
    this.installedApps = list;
    this.currentOrder = currentOrder;
    this.selectedApps = new ArrayList<>();
    this.selectedAppListener = (view, position) -> setSelected(position);
    this.selectedPublisher = PublishSubject.create();
  }

  @Override public AutoUploadAppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AutoUploadAppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app_auto_upload, parent, false), selectedAppListener);
  }

  @Override public void onBindViewHolder(AutoUploadAppViewHolder holder, int position) {
    Log.d("APP-86", "onBindViewHolder: setting app in position " + position);
    holder.setApp(installedApps.get(position), selectedApps.contains(position));
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getItemCount() {
    return installedApps.size();
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

  public void refreshInstalledApps(List<InstalledApp> appsList) {
    installedApps.clear();
    installedApps.addAll(appsList);
    clearAppsSelection(false);
    setOrder(currentOrder);
    notifyDataSetChanged();
  }

  public void setOrder(SortingOrder order) {
    currentOrder = order;
    if (order.equals(SortingOrder.DATE)) {
      Collections.sort(installedApps,
          (obj1, obj2) -> Long.compare(obj2.getInstalledDate(), obj1.getInstalledDate()));
    }
    selectedApps.clear();
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

  public List<InstalledApp> getSelected() {
    List<InstalledApp> selectedAppsList = new ArrayList<>();
    for (Integer appId : selectedApps) {
      selectedAppsList.add(installedApps.get(appId));
    }
    return selectedAppsList;
  }

  private void clearAppsSelection(boolean notify) {
    selectedApps.clear();
    selectedPublisher.onNext(false);
    if (notify) {
      notifyDataSetChanged();
    }
  }

  public void clearAppsSelection() {
    clearAppsSelection(true);
  }
}