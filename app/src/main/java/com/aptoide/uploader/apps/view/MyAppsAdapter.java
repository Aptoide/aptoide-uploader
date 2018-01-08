package com.aptoide.uploader.apps.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

  private final List<InstalledApp> installedApps;
  private final List<Integer> selectedApps;
  private final MyAppsClickListener listener;
  private final PublishSubject<Boolean> selectedPublisher;

  public MyAppsAdapter(@NonNull List<InstalledApp> list) {
    this.installedApps = list;
    this.selectedApps = new ArrayList<>();
    this.listener = (view, position) -> setSelected(position);
    this.selectedPublisher = PublishSubject.create();
  }

  @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app, parent, false), listener);
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
    installedApps.clear();
    selectedApps.clear();
    installedApps.addAll(appsList);
    selectedPublisher.onNext(false);
    notifyDataSetChanged();
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

  public int getSelectedCount() {
    return selectedApps.size();
  }
}
