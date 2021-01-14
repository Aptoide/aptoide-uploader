package com.aptoide.uploader.apps.view;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.glide.GlideApp;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class AutoUploadAppViewHolder extends RecyclerView.ViewHolder {

  private ImageView appIcon;
  private TextView appName;
  private CheckBox checkBox;
  private View background;
  private final AppSelectedListener listener;

  public AutoUploadAppViewHolder(View itemView, AppSelectedListener listener) {
    super(itemView);
    appIcon = itemView.findViewById(R.id.fragment_autoupload_app_icon);
    appName = itemView.findViewById(R.id.fragment_autoupload_app_name);
    checkBox = itemView.findViewById(R.id.fragment_autoupload_app_checkbox);
    background = itemView.findViewById(R.id.fragment_autoupload_app_layout);
    this.listener = listener;
  }

  public void setApp(InstalledApp app, boolean selected) {
    GlideApp.with(itemView)
        .load(app.getAppInfo())
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(appIcon);
    appName.setText(app.getName());

    if (!selected) {
      background.setBackgroundColor(itemView.getResources()
          .getColor(R.color.white));
    } else {
      background.setBackgroundColor(itemView.getResources()
          .getColor(R.color.white_blue));
    }
  }
}