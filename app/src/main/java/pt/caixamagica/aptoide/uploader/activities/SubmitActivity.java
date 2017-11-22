/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.activities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import java.util.ArrayList;
import pt.caixamagica.aptoide.uploader.AptoideUploaderApplication;
import pt.caixamagica.aptoide.uploader.R;
import pt.caixamagica.aptoide.uploader.SelectablePackageInfo;
import pt.caixamagica.aptoide.uploader.SubmitAppFragment;
import pt.caixamagica.aptoide.uploader.util.AppsInStorePersister;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 21-12-2015.
 */
public class SubmitActivity extends ActionBarActivity {

  public static final String FILL_MISSING_INFO = "fillMissingInfo";
  private AppsInStorePersister appsInStorePersister;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));

    Bundle extras = getIntent().getExtras();

    appsInStorePersister = new AppsInStorePersister(this.getApplicationContext()
        .getSharedPreferences(AptoideUploaderApplication.SHARED_PREFERENCES_FILE,
            Context.MODE_PRIVATE));

    UserCredentialsJson userCredentialsJson =
        (UserCredentialsJson) extras.getSerializable("userCredentialsJson");
    PackageInfo packageInfo = ((PackageInfo) extras.getParcelable("selectablePackageInfo"));
    SelectablePackageInfo selectablePackageInfo =
        new SelectablePackageInfo(packageInfo, getPackageManager(),
            appsInStorePersister.isAppInStore(packageInfo.packageName, packageInfo.versionCode));
    String title = extras.getString("title");
    String description = extras.getString("description");
    String languageCode = extras.getString("languageCode");
    boolean fromAppView = extras.getBoolean("fromAppview");
    String category = extras.getString("category");

    switchtoSubmitAppFragment(userCredentialsJson, selectablePackageInfo, title, description,
        languageCode, category);
  }

  private void switchtoSubmitAppFragment(UserCredentialsJson userCredentialsJson,
      SelectablePackageInfo selectablePackageInfo, String title, String description,
      String languageCode, String category) {
    Fragment submitAppFragment = new SubmitAppFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable("userCredentialsJson", userCredentialsJson);

    ArrayList<SelectablePackageInfo> selectablePackageInfos = new ArrayList<>(1);
    selectablePackageInfos.add(selectablePackageInfo);

    bundle.putParcelableArrayList("selectableAppNames", selectablePackageInfos);
    bundle.putString("title", title);
    bundle.putString("description", description);
    bundle.putString("languageCode", languageCode);
    bundle.putBoolean("fromAppview", true);
    bundle.putString("category", category);
    submitAppFragment.setArguments(bundle);

    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, submitAppFragment)
        .commit();
  }
}
