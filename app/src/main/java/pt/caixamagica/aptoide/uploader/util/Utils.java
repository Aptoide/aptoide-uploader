package pt.caixamagica.aptoide.uploader.util;

import java.util.Locale;

/**
 * Created by pedroribeiro on 04/04/17.
 */

public class Utils {

  public static String getLanguage() {
    String language = Locale.getDefault()
        .getLanguage() + "_" + Locale.getDefault()
        .getCountry();
    return language;
  }
}
