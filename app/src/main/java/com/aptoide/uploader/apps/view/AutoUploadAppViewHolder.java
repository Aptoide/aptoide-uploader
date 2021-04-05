package com.aptoide.uploader.apps.view;

import android.net.Uri;
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
  private final ImageView appIcon;
  private final TextView appName;
  private final CheckBox checkBox;
  private final View background;

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
    GlideApp.with(itemView)
        .load(Uri.parse(app.getIconPath()))
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