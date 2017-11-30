package com.aptoide.uploader.apps;

import io.reactivex.Single;

public interface LanguageManager {
  Single<String> getCurrentLanguageCode();
}
