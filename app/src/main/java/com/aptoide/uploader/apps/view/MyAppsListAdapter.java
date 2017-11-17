package com.aptoide.uploader.apps.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.App;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filipe on 17-11-2017.
 */

public class MyAppsListAdapter
    extends android.support.v7.widget.RecyclerView.Adapter<MyAppsListViewHolder> {

  private final List<App> list;

  public MyAppsListAdapter() {
    this.list = new ArrayList<>();
  }

  @Override public MyAppsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    parent.getContext();
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_app, parent, false);
    return new MyAppsListViewHolder(view);
  }

  @Override public void onBindViewHolder(MyAppsListViewHolder holder, int position) {
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
