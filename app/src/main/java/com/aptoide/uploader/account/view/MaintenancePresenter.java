package com.aptoide.uploader.account.view;

import com.aptoide.uploader.account.MaintenanceManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;

public class MaintenancePresenter implements Presenter {

  private MaintenanceView view;
  private MaintenanceNavigator navigator;
  private MaintenanceManager maintenanceManager;

  public MaintenancePresenter(MaintenanceView view, MaintenanceNavigator navigator,
      MaintenanceManager maintenanceManager) {
    this.view = view;
    this.navigator = navigator;
    this.maintenanceManager = maintenanceManager;
  }

  @Override public void present() {

  }


}
