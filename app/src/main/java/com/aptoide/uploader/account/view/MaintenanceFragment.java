package com.aptoide.uploader.account.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.MaintenanceManager;
import com.aptoide.uploader.view.android.FragmentView;

public class MaintenanceFragment extends FragmentView implements MaintenanceView {

  public static MaintenanceFragment newInstance() {
    return new MaintenanceFragment();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    new MaintenancePresenter(this, new MaintenanceNavigator(), new MaintenanceManager()).present();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.frament_maintenance, container, false);
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Override public void showMaintenanceView() {
    // TODO: 24/04/2020 show maintenance whole view
  }
}
