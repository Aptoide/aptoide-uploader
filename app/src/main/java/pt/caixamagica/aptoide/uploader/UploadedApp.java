package pt.caixamagica.aptoide.uploader;

/**
 * Created by filipe on 13-11-2017.
 */

public class UploadedApp {
  private String packageName;
  private int vercode;

  public UploadedApp(String packageName, int vercode) {
    this.packageName = packageName;
    this.vercode = vercode;
  }

  public String getPackageName() {
    return packageName;
  }

  public int getVercode() {
    return vercode;
  }
}
