package pt.caixamagica.aptoide.uploader.util;

import android.content.Context;
import java.util.List;
import lombok.Data;
import pt.caixamagica.aptoide.uploader.SelectablePackageInfo;

/**
 * Created by neuro on 12-10-2017.
 */

public class Md5AsyncUtils {

  private final Context context;
  private final InstalledUtils installedUtils;

  public Md5AsyncUtils(Context context, InstalledUtils installedUtils) {
    this.context = context.getApplicationContext();
    this.installedUtils = installedUtils;
  }

  public void computeMd5(List<SelectablePackageInfo> selectablePackageInfos,
      OnNewUploadedApps onNewUploadedApps) {
    for (SelectablePackageInfo selectablePackageInfo : selectablePackageInfos) {
      String md5sum = AlgorithmUtils.computeMd5(selectablePackageInfo);
      //// TODO: 15-11-2017 filipe check if already checked this app.
      onNewUploadedApps.onNewUploadedApps(Model.from(selectablePackageInfo, md5sum));
    }
  }

  public interface OnNewUploadedApps {
    void onNewUploadedApps(Model model);
  }

  @Data public static class Model {

    private final String packageName;
    private final int verCode;
    private final String md5sum;

    public Model(String packageName, int verCode, String md5sum) {
      this.packageName = packageName;
      this.verCode = verCode;
      this.md5sum = md5sum;
    }

    public static Model from(SelectablePackageInfo selectablePackageInfo, String md5sum) {

      String packageName = selectablePackageInfo.packageName;
      int verCode = selectablePackageInfo.versionCode;
      return new Model(packageName, verCode, md5sum);
    }
  }
}
