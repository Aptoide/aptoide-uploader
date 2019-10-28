package com.aptoide.uploader.apps.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import com.bumptech.glide.Glide;

public class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

  private final ImageView image;
  private final TextView appName;
  private final View background;
  private final AppSelectedListener listener;
  private final AppCompatImageView cloud;

  AppViewHolder(View itemView, AppSelectedListener listener) {
    super(itemView);
    image = itemView.findViewById(R.id.item_app_icon);
    appName = itemView.findViewById(R.id.item_app_name);
    background = itemView.findViewById(R.id.item_app_layout);
    cloud = itemView.findViewById(R.id.appInCloud);
    this.listener = listener;
    itemView.setOnClickListener(this);
  }

  void setApp(InstalledApp app, boolean selected) {
    Glide.with(itemView)
        .load(app.getIcon())
        .placeholder(new ColorDrawable(Color.parseColor("#EDEEF2")))
        .into(image);
    appName.setText(app.getName());
    if (!selected) {
      background.setBackgroundColor(itemView.getResources()
          .getColor(R.color.white));
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        background.setBackground(itemView.getResources()
            .getDrawable(R.drawable.overlay_focused));
      } else {
        background.setBackgroundDrawable(itemView.getResources()
            .getDrawable(R.drawable.overlay_focused));
      }
    }
    if (app.isUploaded()) {
      cloud.setVisibility(View.VISIBLE);
    } else {
      cloud.setVisibility(View.GONE);
    }
  }

  @Override public void onClick(View view) {
    listener.onClick(view, getAdapterPosition());
  }
}
