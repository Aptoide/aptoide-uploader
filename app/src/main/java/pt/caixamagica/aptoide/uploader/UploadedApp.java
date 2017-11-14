package pt.caixamagica.aptoide.uploader;

/**
 * Created by filipe on 13-11-2017.
 */

public class UploadedApp {
  private String packageName;
  private long vercode;

  public UploadedApp(String packageName, long vercode) {
    this.packageName = packageName;
    this.vercode = vercode;
  }

  public String getPackageName() {
    return packageName;
  }

  public long getVercode() {
    return vercode;
  }
}
