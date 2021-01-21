package com.aptoide.uploader.apps.view;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.glide.GlideApp;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class AutoUploadAppViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener {

  private final AppSelectedListener listener;
  private ImageView appIcon;
  private TextView appName;
  private CheckBox checkBox;
  private View background;

  public AutoUploadAppViewHolder(View itemView, AppSelectedListener listener) {
    super(itemView);
    this.appIcon = itemView.findViewById(R.id.fragment_autoupload_app_icon);
    this.appName = itemView.findViewById(R.id.fragment_autoupload_app_name);
    this.checkBox = itemView.findViewById(R.id.fragment_autoupload_app_checkbox);
    this.background = itemView.findViewById(R.id.fragment_autoupload_app_layout);
    this.listener = listener;
    itemView.setOnClickListener(this);
  }

  public void setApp(InstalledApp app, boolean selected) {
    Log.d("APP-86", "setApp: setapp " + app.getName());
    GlideApp.with(itemView)
        .load(app.getAppInfo())
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(appIcon);
    appName.setText(app.getName());
    checkBox.setChecked(selected);
    if (!selected) {
      background.setBackgroundColor(itemView.getResources()
          .getColor(R.color.white));
    } else {
      background.setBackgroundColor(itemView.getResources()
          .getColor(R.color.white_blue));
    }
  }

  @Override public void onClick(View view) {
    listener.onClick(view, getAdapterPosition());
  }
}