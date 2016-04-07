/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.activities;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import pt.caixamagica.aptoide.uploader.R;
import pt.caixamagica.aptoide.uploader.SelectablePackageInfo;
import pt.caixamagica.aptoide.uploader.SubmitAppFragment;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 21-12-2015.
 */
public class SubmitActivity extends ActionBarActivity {

	public static final String FILL_MISSING_INFO = "fillMissingInfo";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));

		Bundle extras = getIntent().getExtras();

		UserCredentialsJson userCredentialsJson = (UserCredentialsJson) extras.getSerializable("userCredentialsJson");
		SelectablePackageInfo selectablePackageInfo = new SelectablePackageInfo(((PackageInfo) extras.getParcelable("selectablePackageInfo")), getPackageManager());

		switchtoSubmitAppFragment(userCredentialsJson, selectablePackageInfo);
	}

	private void switchtoSubmitAppFragment(UserCredentialsJson userCredentialsJson, SelectablePackageInfo selectablePackageInfo) {
		Fragment submitAppFragment = new SubmitAppFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("userCredentialsJson", userCredentialsJson);

		ArrayList<SelectablePackageInfo> selectablePackageInfos = new ArrayList<>(1);
		selectablePackageInfos.add(selectablePackageInfo);

		bundle.putParcelableArrayList("selectableAppNames", selectablePackageInfos);
		submitAppFragment.setArguments(bundle);

		getSupportFragmentManager().beginTransaction().replace(R.id.container, submitAppFragment).commit();
	}
}
