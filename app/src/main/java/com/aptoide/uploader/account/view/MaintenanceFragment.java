package com.aptoide.uploader.account.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.view.android.FragmentView;
import io.reactivex.disposables.CompositeDisposable;

public class MaintenanceFragment extends FragmentView implements MaintenanceView {

  private TextView testTextView;

  public static MaintenanceFragment newInstance() {
    return new MaintenanceFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    testTextView = view.findViewById(R.id.testString);

    new MaintenancePresenter(this, new MaintenanceNavigator(),
        ((UploaderApplication) getContext().getApplicationContext()).getMaintenanceManager(),
        new CompositeDisposable()).present();
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

  @Override public void showNoLoginView() {
    testTextView.setText("NO SOCIAL");
  }

  @Override public void showSocialLoginMaintenanceView() {
    testTextView.setText("SHOW SOCIAL !!! :) ");
  }
}
