package com.aptoide.uploader.glide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule public class MyAppGlideModule extends AppGlideModule {

  @Override public void registerComponents(@NonNull Context context, @NonNull Glide glide,
      @NonNull Registry registry) {
    registry.prepend(ApplicationInfo.class, Drawable.class,
        new DrawableModelLoaderFactory(context));
  }
}