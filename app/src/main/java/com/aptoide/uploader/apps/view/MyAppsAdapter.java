package com.aptoide.uploader.apps.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

  private final List<InstalledApp> installedApps;
  private final List<Integer> selectedApps;
  private final AppSelectedListener selectedAppListener;
  private final AppLongClickListener longClickListener;
  private final PublishSubject<Boolean> selectedPublisher;

  public MyAppsAdapter(@NonNull List<InstalledApp> list, AppLongClickListener longClickListener) {
    this.installedApps = list;
    this.longClickListener = longClickListener;
    this.selectedApps = new ArrayList<>();
    this.selectedAppListener = (view, position) -> setSelected(position);
    this.selectedPublisher = PublishSubject.create();
  }

  @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app, parent, false), selectedAppListener, longClickListener);
  }

  @Override public void onBindViewHolder(AppViewHolder holder, int position) {
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

  public void setInstalledApps(List<InstalledApp> appsList) {
    if (!appsList.equals(installedApps)) {
      installedApps.clear();
      installedApps.addAll(appsList);
      clearAppsSelection(false);
      setOrder(SortingOrder.DATE);
    }
  }

  public void refreshInstalledApps(List<InstalledApp> appsList) {
    installedApps.clear();
    installedApps.addAll(appsList);
    clearAppsSelection(false);
    setOrder(SortingOrder.DATE);
  }

  public void setOrder(SortingOrder order) {
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

  public void setCloudIcon(List<String> uploadedPackageNames) {
    for (int i = 0; i < installedApps.size(); i++) {
      boolean setted = false;
      InstalledApp app = installedApps.get(i);
      for (String packageName : uploadedPackageNames) {
        if (app.getPackageName()
            .equals(packageName)) {
          app.setIsUploaded(true);
          setted = true;
        }
      }
      if (!setted) {
        app.setIsUploaded(false);
      }
    }
    notifyDataSetChanged();
  }
}
