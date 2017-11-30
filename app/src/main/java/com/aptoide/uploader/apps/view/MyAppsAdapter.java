package com.aptoide.uploader.apps.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.App;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

  private final List<App> list;

  public MyAppsAdapter(@NonNull List<App> list) {
    this.list = list;
  }

  @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AppViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app, parent, false));
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
}
