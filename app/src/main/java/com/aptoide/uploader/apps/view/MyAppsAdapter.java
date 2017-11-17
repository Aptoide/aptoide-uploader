package com.aptoide.uploader.apps.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.App;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

  private final List<App> list;
  private final LayoutInflater inflater;

  public MyAppsAdapter(LayoutInflater inflater, List<App> list) {
    this.inflater = inflater;
    this.list = list;
  }

  @Override public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    parent.getContext();
    return new AppViewHolder(inflater.inflate(R.layout.item_app, parent, false));
  }

  @Override public void onBindViewHolder(AppViewHolder holder, int position) {
    holder.setApp(list.get(position));
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
