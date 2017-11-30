package com.aptoide.uploader.apps.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.App;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

  private final PublishSubject<App> appClickSubject;
  private final List<App> list;

  public MyAppsAdapter(@NonNull List<App> list, @NonNull PublishSubject<App> appClickSubject) {
    this.list = list;
    this.appClickSubject = appClickSubject;
  }

  @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app, parent, false), appClickSubject);
  }

  @Override public void onBindViewHolder(AppViewHolder holder, int position) {
    holder.setApp(list.get(position));
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getItemCount() {
    return list.size();
  }

  public void setList(List<App> appsList) {
    list.clear();
    list.addAll(appsList);
    notifyDataSetChanged();
  }

  public Observable<App> listenForAppClicks() {
    return appClickSubject;
  }
}
