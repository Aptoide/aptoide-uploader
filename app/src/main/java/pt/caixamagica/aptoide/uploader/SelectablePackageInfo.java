/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by neuro on 05-02-2015.
 */

/**
 * This class contains content from the submitappfragment to be sent with the apk on upload
 */
@Getter @Setter public class SelectablePackageInfo extends PackageInfo {

  private final PackageInfo info;

  public boolean selected;

  private PackageManager pm;

  private String label;

  // Descrições e etcs..
  private String name;

  private int ageRating;

  private int category;

  private String description;

  private String phoneNumber;

  private String email;

  private String website;

  private String inputTitle;
      //For proposed translations only - when a title comes from getProposed webservice
      private String lang;

  public SelectablePackageInfo(PackageInfo info, PackageManager pm) {
    super();
    this.pm = pm;
    this.info = info;

    if (info != null) {
      this.packageName = info.packageName;
      this.applicationInfo = info.applicationInfo;
      this.firstInstallTime = info.firstInstallTime;

      this.versionCode = info.versionCode;
      this.versionName = info.versionName;
    }
  }

  public boolean isSelected() {
    return selected;
  }

  public void toggleSelected() {
    selected = !selected;
  }

  public String getLabel() {
    if (label == null) loadLabel();
    return label;
  }

  private void loadLabel() {
    label = (String) applicationInfo.loadLabel(pm);
  }

  public String getApkPath() {
    if (info != null && info.applicationInfo != null) {
      return info.applicationInfo.sourceDir;
    } else {
      return null;
    }
  }

  public int tes() {
    return applicationInfo.labelRes;
  }
}