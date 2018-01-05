package com.aptoide.uploader.apps.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

  private final List<InstalledApp> list;
  private final List<Integer> selectionList;
  private final MyAppsClickListener listener;
  private final PublishSubject<Boolean> selectedPublisher;

  public MyAppsAdapter(@NonNull List<InstalledApp> list) {
    this.list = list;
    this.selectionList = new ArrayList<>();
    this.listener= (view, position) -> setSelected(position);
    this.selectedPublisher = PublishSubject.create();
  }

  @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app, parent, false), listener);
  }

  @Override public void onBindViewHolder(AppViewHolder holder, int position) {
    holder.setApp(list.get(position), selectionList.contains(position));
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getItemCount() {
    return list.size();
  }

  public Observable<Boolean> toggleSelection(){
    return selectedPublisher;
  }
  public void setList(List<InstalledApp> appsList) {
    list.clear();
    selectionList.clear();
    list.addAll(appsList);
    notifyDataSetChanged();
  }

  public void handleBackNavigation(){
    selectionList.clear();
    notifyDataSetChanged();
  }

  public void setSelected(int position){
    if(selectionList.contains(position)) {
      selectionList.remove((Integer) position);
      if(selectionList.size()==0)
        selectedPublisher.onNext(false);
      else
        selectedPublisher.onNext(true);
    }
    else {
      selectionList.add(position);
      selectedPublisher.onNext(true);
    }
    notifyItemChanged(position);
  }

  public int getSelectedCount(){
    return selectionList.size();
  }
}
