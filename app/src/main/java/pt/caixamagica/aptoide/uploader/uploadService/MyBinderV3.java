/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.uploadService;

import android.os.Binder;

/**
 * Created by neuro on 07-03-2015.
 */
public class MyBinderV3 extends Binder {

	UploadServiceV3 uploadServiceV3;

	public MyBinderV3(UploadServiceV3 uploadServiceV3) {
		this.uploadServiceV3 = uploadServiceV3;
	}

	public UploadServiceV3 getService() {
		return uploadServiceV3;
	}
}
