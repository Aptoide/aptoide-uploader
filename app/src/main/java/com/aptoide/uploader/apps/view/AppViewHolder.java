package com.aptoide.uploader.apps.view;

import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.AppUploadStatus;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.glide.GlideApp;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class AppViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener, View.OnLongClickListener {

  private final ImageView image;
  private final TextView appName;
  private final View background;
  private final AppSelectedListener listener;
  private final AppLongClickListener longClickListener;
  private final AppCompatImageView cloud;
  private String packageName;

  AppViewHolder(View itemView, AppSelectedListener listener,
      AppLongClickListener longClickListener) {
    super(itemView);
    image = itemView.findViewById(R.id.item_app_icon);
    appName = itemView.findViewById(R.id.item_app_name);
    background = itemView.findViewById(R.id.item_app_layout);
    cloud = itemView.findViewById(R.id.appInCloud);
    this.listener = listener;
    this.longClickListener = longClickListener;
    itemView.setOnClickListener(this);
    itemView.setOnLongClickListener(this);
  }

  void setApp(InstalledApp app, boolean selected, AppUploadStatus uploadStatus) {
    packageName = app.getPackageName();
    GlideApp.with(itemView)
        .load(Uri.parse(app.getIconPath()))
        .transition(DrawableTransitionOptions.withCrossFade())
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
    if (uploadStatus != null && uploadStatus.isUploaded()) {
      cloud.setVisibility(View.VISIBLE);
    } else {
      cloud.setVisibility(View.GONE);
    }
  }

  @Override public void onClick(View view) {
    listener.onClick(view, getAdapterPosition());
  }

  @Override public boolean onLongClick(View v) {
    longClickListener.onLongClick(v, packageName);
    return true;
  }
}
