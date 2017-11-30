package com.aptoide.uploader.apps.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.App;
import com.bumptech.glide.Glide;
import io.reactivex.subjects.PublishSubject;

public class AppViewHolder extends RecyclerView.ViewHolder {

  private final View layout;
  private final ImageView image;
  private final TextView appName;
  private final PublishSubject<App> appClickSubject;

  public AppViewHolder(View itemView, PublishSubject<App> appClickSubject) {
    super(itemView);
    image = itemView.findViewById(R.id.item_app_icon);
    appName = itemView.findViewById(R.id.item_app_name);
    layout = itemView.findViewById(R.id.item_app_layout);
    this.appClickSubject = appClickSubject;
  }

  public void setApp(App app) {
    Glide.with(itemView)
        .load(app.getIcon())
        .into(image);
    appName.setText(app.getName());

    itemView.setOnClickListener(__ -> {
      // should I modify the app state here?...
      app.setSelected(!app.isSelected());
      appClickSubject.onNext(app);
      final int color = layout.getResources()
          .getColor(app.isSelected() ? R.color.red : R.color.white);
      layout.setBackgroundColor(color);
    });
  }
}
