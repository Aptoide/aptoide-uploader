package com.aptoide.uploader.apps.view;

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
  private final MyAppsClickListener listener;

  public AppViewHolder(View itemView, MyAppsClickListener listener) {
    super(itemView);
    image = itemView.findViewById(R.id.item_app_icon);
    appName = itemView.findViewById(R.id.item_app_name);
    background = itemView.findViewById(R.id.item_app_layout);
    this.listener = listener;
    itemView.setOnClickListener(this);
  }

  public void setApp(InstalledApp app, boolean set) {
    Glide.with(itemView)
        .load(app.getIcon())
        .into(image);
    appName.setText(app.getName());
    if(!set){
      background.setBackgroundColor(itemView.getResources().getColor(R.color.white));
    }
    else{
      background.setBackground(itemView.getResources().getDrawable(R.drawable.overlay_focused));
    }
  }


  @Override public void onClick(View view) {
    listener.onClick(view, getAdapterPosition());
  }
}
