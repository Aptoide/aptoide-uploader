package com.aptoide.uploader.apps.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.App;
import com.aptoide.uploader.apps.AppsManager;
import com.aptoide.uploader.apps.PackageManagerProvider;
import com.aptoide.uploader.view.android.FragmentView;
import io.reactivex.disposables.CompositeDisposable;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Created by trinkes on 17/11/2017.
 */

public class MyAppsFragment extends FragmentView implements MyAppsView {

  public static MyAppsFragment newInstance() {
    return new MyAppsFragment();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_app_view, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    new MyAppsPresenter(this, new AppsManager(new PackageManagerProvider()),
        new CompositeDisposable()).present();
  }

  @Override public void showApps(@NotNull List<App> appsList) {
    Toast.makeText(getContext(), "Lots of apps", Toast.LENGTH_SHORT)
        .show();
  }
}
