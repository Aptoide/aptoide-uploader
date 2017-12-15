package pt.caixamagica.aptoide.uploader.util;

import android.content.SharedPreferences;
import java.util.List;
import pt.caixamagica.aptoide.uploader.UploadedApp;

/**
 * Created by filipe on 13-11-2017.
 */

public class AppsInStorePersister {

  private final SharedPreferences sharedPreferences;

  public AppsInStorePersister(SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  public void saveUploadedApps(List<UploadedApp> listOfStoreApps) {
    for (int i = 0; i < listOfStoreApps.size(); i++) {
      addUploadedAppToSharedPreferences(listOfStoreApps.get(i)
          .getPackageName(), listOfStoreApps.get(i)
          .getVercode());
    }
  }

  public boolean isAppInStore(String packageName, int versionCode) {
    return sharedPreferences.getInt(packageName, Integer.MAX_VALUE) == versionCode;
  }

  public void addUploadedAppToSharedPreferences(String packageName, int versionCode) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putInt(packageName, versionCode);
    editor.apply();
  }
}
