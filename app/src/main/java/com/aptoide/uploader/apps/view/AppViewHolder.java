package com.aptoide.uploader.apps.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import com.bumptech.glide.Glide;

public class AppViewHolder extends RecyclerView.ViewHolder {

  private final ImageView image;
  private final TextView appName;

  public AppViewHolder(View itemView) {
    super(itemView);
    image = itemView.findViewById(R.id.item_app_icon);
    appName = itemView.findViewById(R.id.item_app_name);
  }

  public void setApp(InstalledApp app) {
    Glide.with(itemView)
        .load(app.getIcon())
        .into(image);
    appName.setText(app.getName());
  }
}
