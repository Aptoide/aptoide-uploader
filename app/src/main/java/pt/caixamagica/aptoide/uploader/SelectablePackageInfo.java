/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcelable;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by neuro on 05-02-2015.
 */
@Getter
@Setter
public class SelectablePackageInfo extends PackageInfo implements Parcelable {

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

	public SelectablePackageInfo(PackageInfo info, PackageManager pm) {
		super();
		this.pm = pm;

		this.packageName = info.packageName;
		this.info = info;
		this.applicationInfo = info.applicationInfo;
		this.firstInstallTime = info.firstInstallTime;

		this.versionCode = info.versionCode;
		this.versionName = info.versionName;
	}

	public boolean isSelected() {
		return selected;
	}

	public void toggleSelected() {
		selected = !selected;
	}

	private void loadLabel() {
		label = (String) applicationInfo.loadLabel(pm);
	}

	public String getLabel() {
		if (label == null) loadLabel();
		return label;
	}

	public String getApkPath() {
		return info.applicationInfo.sourceDir;
	}

	public int tes() {
		return applicationInfo.labelRes;
	}
}