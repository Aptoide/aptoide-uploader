package pt.caixamagica.aptoide.uploader.util;

/**
 * Created by pedroribeiro on 21/04/17.
 */

public class LanguageCodesHelper {

  public static String translateToLanguageName(String languageCode) {
    switch (languageCode) {
      case "ar":
        return "العربية";
      case "bn":
        return "বাংলা";
      case "my":
        return "ဗမာစာ";
      case "de":
        return "Deutsch";
      case "es":
        return "Español";
      case "fa":
        return "فارسی";
      case "fil":
        return "Filipino";
      case "fr":
        return "Français";
      case "hi":
        return "हिंदी";
      case "id":
        return "Bahasa Indonesia";
      case "it":
        return "Italiano";
      case "ml":
        return "Bahasa Melayu";
      case "pt_PT":
        return "Português";
      case "pt_BR":
        return "Português (Brasil)";
      case "ru":
        return "Русский";
      case "th":
        return "ภาษาไทย";
      case "tr":
        return "Türk";
      case "vi":
        return "Tiếng Việt";
      case "zh_CN":
        return "中文";
      default:
        return "English";
    }
  }

  public static String translateToLanguageCode(String languageName) {
    switch (languageName) {
      case "العربية":
        return "ar";
      case "বাংলা":
        return "bn";
      case "ဗမာစာ":
        return "my";
      case "Deutsch":
        return "de";
      case "Español":
        return "es";
      case "فارسی":
        return "fa";
      case "Filipino":
        return "fil";
      case "Français":
        return "fr";
      case "हिंदी":
        return "hi";
      case "Bahasa Indonesia":
        return "id";
      case "Italiano":
        return "it";
      case "Bahasa Melayu":
        return "ml";
      case "Português":
        return "pt_PT";
      case "Português (Brasil)":
        return "pt_BR";
      case "Русский":
        return "ru";
      case "ภาษาไทย":
        return "th";
      case "Türk":
        return "tr";
      case "Tiếng Việt":
        return "vi";
      case "中文":
        return "zh_CN";
      default:
        return "en";
    }
  }
}
