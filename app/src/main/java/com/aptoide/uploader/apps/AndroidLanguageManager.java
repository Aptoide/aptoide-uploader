package com.aptoide.uploader.apps;

import io.reactivex.Single;
import java.util.Locale;

/**
 * Created by trinkes on 27/12/2017.
 */

public class AndroidLanguageManager implements LanguageManager {
  @Override public Single<String> getCurrentLanguageCode() {
    return Single.just(Locale.getDefault()
        .getLanguage());
  }
}
